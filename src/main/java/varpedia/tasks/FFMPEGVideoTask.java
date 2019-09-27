package varpedia.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;

import javafx.concurrent.Task;
import varpedia.Command;

public class FFMPEGVideoTask extends Task<Void> {

	private static List<String> search(String term, int count) throws FlickrException {
		List<String> list = new ArrayList<String>();
		Flickr f = new Flickr("9babcbe979fd6fadead2c06ad346bbc2", "b690a901bd52d58d", new REST());
		PhotosInterface photos = f.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setText(term);
		params.setSort(SearchParameters.RELEVANCE);
		params.setMedia("photos");
		PhotoList<Photo> photoList = photos.search(params, count, 0);
			
		for (Photo p : photoList) {
			list.add("https://farm" + p.getFarm() + ".staticflickr.com/" + p.getServer() + "/" + p.getId() + "_" + p.getSecret() + ".jpg");
		}
		
		return list;
	}
	
	private static void downloadImage(URL source, File dest) throws IOException {
		try (InputStream input = source.openStream(); FileOutputStream output = new FileOutputStream(dest)) {
			byte[] transfer = new byte[4096];
			int count;
			
			while ((count = input.read(transfer)) != -1) {
				output.write(transfer, 0, count);
			}
		}
	}
	
	private String[] _chunks;
	private String _term;
	private String _creation;
	private int _images;
	
	public FFMPEGVideoTask(String term, String creation, int images, String[] chunks) {
		_chunks = chunks;
		_term = term;
		_creation = creation;
		_images = images;
	}
	
	@Override
	protected Void call() throws Exception {
		
		List<String> list = search(_term, _images);
		int id = 0;
		
		for (String url : list) {
			System.out.println(url);
			downloadImage(new URL(url), new File("appfiles/image" + Integer.toString(id) + ".jpg"));
			id++;
		}
		
		Command audio = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", "appfiles/audio.wav");
		audio.run();
		
		for (String s : _chunks) {
			audio.writeString("file 'appfiles/audio/" + s.replace("'", "'\\''") + ".wav'");
		}
		
		audio.getProcess().getOutputStream().close();
		audio.getError(); //FFmpeg needs its stderr to be emptied
		
		if (audio.getProcess().waitFor() != 0) {
			throw new Exception("Failed to merge audio " + audio.getProcess().exitValue());
		}
		
		AudioFileFormat file = AudioSystem.getAudioFileFormat(new File("appfiles/audio.wav"));
		float length = file.getFrameLength() / file.getFormat().getFrameRate();
		
		Command video = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=text='" + _term + "':x=(w-text_w)/2:y=(h-text_h)/2:fontsize=72:borderw=2:bordercolor=white:expansion=none", "creations/" + _creation + ".mp4");
		video.run();
		
		for (int i = 0; i < id; i++) {
			video.writeString("file '" + "appfiles/image" + Integer.toString(i) + ".jpg" + "'");
			video.writeString("duration " + (length / id));
		}
		
		video.getProcess().getOutputStream().close();
		video.getError(); //FFmpeg needs its stderr to be emptied
		
		if (video.getProcess().waitFor() != 0) {
			throw new Exception("Failed to merge video" + video.getProcess().exitValue());
		}
		
		//Create audio: input concat file | ffmpeg -f concat -protocol_whitelist file,pipe -i - <output>
		//Create video: input concat file | ffmpeg -f concat -protocol_whitelist file,pipe -i - -i <audio> -vf "scale=w=min(iw*540/ih\,960):h=min(540\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=text='<text>':x=(w-text_w)/2:y=(h-text_h)/2:fontsize=72:borderw=2:bordercolor=white:expansion=none" <output>

		return null;
	}

}
