package varpedia.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import javafx.concurrent.Task;
import varpedia.Command;

public class FFMPEGVideoTask extends Task<Void> {
	
	private List<String> _chunks;
	private String _creation;
	private List<Integer> _images;
	
	public FFMPEGVideoTask(String creation, List<Integer> images, List<String> chunks) {
		_chunks = chunks;
		_creation = creation;
		_images = images;
	}
	
	@Override
	protected Void call() throws Exception {

		Command audio = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", "appfiles/audio.wav");
		audio.run();
		
		for (String s : _chunks) {
			if (isCancelled()) {
				audio.end();
				return null;
			}
			
			audio.writeString("file 'appfiles/audio/" + s.replace("'", "'\\''") + ".wav'");
		}
		
		audio.getProcess().getOutputStream().close();
		new Thread(() -> {audio.getError();}).start(); //FFmpeg needs its stderr to be emptied

		try {
			if (audio.getProcess().waitFor() != 0) {
				throw new Exception("Failed to merge audio " + audio.getProcess().exitValue());
			}
		} catch (InterruptedException e) {
			audio.end();
			return null;
		}
		
		AudioFileFormat file = AudioSystem.getAudioFileFormat(new File("appfiles/audio.wav"));
		float length = file.getFrameLength() / file.getFormat().getFrameRate();
		Command video;
		boolean catJpgMode = false;
		
		if (_images.size() > 2) {
			video = new Command("ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25", "creations/" + _creation + ".mp4");
		} else if (_images.size() > 0) {
			//FFMPEG and MediaPlayer do not work properly on Linux with 1-2 images.
			video = new Command("ffmpeg", "-y", "-f", "image2pipe", "-framerate", Float.toString(length / 2), "-i", "-", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25", "creations/" + _creation + ".mp4");
			catJpgMode = true;
		} else {
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
			
		try {
			if (video.getProcess().waitFor() != 0) {
				throw new Exception("Failed to merge video" + video.getProcess().exitValue());
			}
		} catch (InterruptedException e) {
			video.end();
			return null;
		}
		
		//Create audio: input concat file | ffmpeg -f concat -protocol_whitelist file,pipe -i - <output>
		//Create video: input concat file | ffmpeg -f concat -protocol_whitelist file,pipe -i - -i <audio> -vf "scale=w=min(iw*540/ih\,960):h=min(540\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=text='<text>':x=(w-text_w)/2:y=(h-text_h)/2:fontsize=72:borderw=2:bordercolor=white:expansion=none" <output>

		return null;
	}

}
