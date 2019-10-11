package varpedia.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import javafx.concurrent.Task;
import varpedia.Command;

/**
 * Background task that handles all FFMPEG commands for creation creation - including concatenation of audio chunks,
 * and merging of audio with Flickr images to produce the final creation video.
 *
 * Author: Tudor Zagreanu
 */
public class FFMPEGVideoTask extends Task<Void> {
	
	private List<String> _chunks;
	private String _creation;
	private List<Integer> _images;
	private String _background;
	private double _volume;
	
	/**
	 * @param creation The name of the creation
	 * @param images The list of image indices to create. These refer to filenames appfiles/image&ltid&gt.jpg
	 * @param chunks The list of chunk names. These refer to filenames appfiles/audio/&ltchunk name&gt.wav
	 * @param background The background music in a file
	 * @param volume The volume of the background music
	 */
	public FFMPEGVideoTask(String creation, List<Integer> images, List<String> chunks, String background, double volume) {
		_chunks = chunks;
		_creation = creation;
		_images = images;
		_background = background;
		_volume = volume;
	}
	
	@Override
	protected Void call() throws Exception {

		//Concatenate audio files into a single audio file at appfiles/preaudio.wav or appfiles/audio.wav
		Command audio = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", _background == null ? "appfiles/audio.wav" : "appfiles/preaudio.wav");
		audio.run();
		
		//Pipe the chunk names in
		for (String s : _chunks) {
			if (isCancelled()) {
				audio.end();
				return null;
			}
			
			audio.writeString("file 'appfiles/audio/" + s.replace("'", "'\\''") + ".wav'");
		}
		
		audio.getProcess().getOutputStream().close();
		new Thread(() -> {audio.getError();}).start(); //FFmpeg needs its stderr to be emptied

		//Wait for it to be done
		try {
			if (audio.getProcess().waitFor() != 0) {
				throw new Exception("Failed to merge audio " + audio.getProcess().exitValue());
			}
		} catch (InterruptedException e) {
			audio.end();
			return null;
		}
		
		//Now mix it with the music into appfiles/audio.wav
		if (_background != null) {
			Command mixer = new Command("ffmpeg", "-y", "-i", "appfiles/preaudio.wav", "-i", "-", "-filter_complex", "[1]volume=volume=" + _volume + "[a];[0][a]amix=inputs=2:duration=first", "appfiles/audio.wav");
			mixer.run();
			
			//Pipe the audio in
			try (InputStream in = PlayChunkTask.class.getResourceAsStream(_background)) {
				byte[] transfer = new byte[4096];
				int count;
				
				while ((count = in.read(transfer)) != -1) {
					if (isCancelled()) {
						return null;
					}
					
					try {
						mixer.getProcess().getOutputStream().write(transfer, 0, count);
					} catch (IOException e) {
						//Very ugly hack. Basically FFmpeg doesn't bother reading the whole pipe, and will close the pipe ("thanks")
						//There is no easy way to detect this in Java, basically you have to catch an IOException and hope you get the right one.
						//On Windows the message is "The pipe has been ended", but I don't know the Linux one and I cannot be bothered to find out.
						//So just exit the loop.
						break;
					}
				}
			}
			
			try {
				mixer.getProcess().getOutputStream().close();
			} catch (IOException e) {
				; //As a result of ugly hack #1, ugly hack #2 is needed because otherwise, "The pipe is being closed"
			}
				
			new Thread(() -> {mixer.getError();}).start(); //FFmpeg needs its stderr to be emptied
			
			//Wait for it to be done
			try {
				if (mixer.getProcess().waitFor() != 0) {
					throw new Exception("Failed to mix audio " + mixer.getProcess().exitValue());
				}
			} catch (InterruptedException e) {
				mixer.end();
				return null;
			}
		}
		
		//Get the length of the audio file
		AudioFileFormat file = AudioSystem.getAudioFileFormat(new File("appfiles/audio.wav"));
		float length = file.getFrameLength() / file.getFormat().getFrameRate();
		Command video;
		boolean catJpgMode = false;
		
		if (_images.size() > 2) {
			//The normal method. Works with 3+ images (and 2 images on Windows).
			//The command takes care of scaling images to a 960x540 yuv420p 25fps video, and draws the search term in appfiles/search-term.txt with a white border 3/4 of the way down the video.
			video = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25", "creations/" + _creation + ".mp4");
		} else if (_images.size() > 0) {
			//FFMPEG and MediaPlayer do not work properly on Linux with 1-2 images.
			video = new Command("ffmpeg", "-y", "-f", "image2pipe", "-framerate", "1/" + Float.toString(length / 2), "-i", "-", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25", "creations/" + _creation + ".mp4");
			catJpgMode = true;
		} else {
			//Method for no images
			video = new Command("ffmpeg", "-y", "-f", "lavfi", "-t", Float.toString(length), "-i", "color=color=white:size=960x540", "-i", "appfiles/audio.wav", "-vf", "drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:expansion=none", "-pix_fmt", "yuv420p", "-r", "25", "creations/" + _creation + ".mp4");
		}
		
		video.run();
		
		if (catJpgMode) {
			//FFMPEG and MediaPlayer do not work properly on Linux with 1-2 images.
			int[] imgs = new int[2];
			
			if (_images.size() > 1) {
				imgs[0] = _images.get(0);
				imgs[1] = _images.get(1);
			} else {
				//1 is not enough, it needs 2
				imgs[0] = _images.get(0);
				imgs[1] = _images.get(0);
			}
			
			for (int i : imgs) {
				//Pipe the image data in
				try (InputStream in = new FileInputStream("appfiles/image" + Integer.toString(i) + ".jpg")) {
					byte[] transfer = new byte[4096];
					int count;
					
					while ((count = in.read(transfer)) != -1) {
						if (isCancelled()) {
							return null;
						}
						
						video.getProcess().getOutputStream().write(transfer, 0, count);
					}
				}
			}
		} else {
			//Pipe the image names in
			for (int i : _images) {
				if (isCancelled()) {
					video.end();
					return null;
				}
				
				video.writeString("file '" + "appfiles/image" + Integer.toString(i) + ".jpg" + "'");
				video.writeString("duration " + (length / _images.size()));
			}
		}
		
		video.getProcess().getOutputStream().close();
		new Thread(() -> {video.getError();}).start(); //FFmpeg needs its stderr to be emptied

		//Wait for it to be done
		try {
			if (video.getProcess().waitFor() != 0) {
				throw new Exception("Failed to merge video" + video.getProcess().exitValue());
			}
		} catch (InterruptedException e) {
			video.end();
			return null;
		}

		return null;
	}

}
