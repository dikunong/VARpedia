package varpedia.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import javafx.concurrent.Task;
import varpedia.FFMPEGCommand;

/**
 * Background task that handles all FFMPEG commands for creation creation - including concatenation of audio chunks,
 * and merging of audio with Flickr images to produce the final creation video.
 *
 * @author Tudor Zagreanu
 */
public class FFMPEGVideoTask extends Task<Void> {

	private String _creation;
	private List<Integer> _images;
	
	/**
	 * @param creation The name of the creation (or null if previewing)
	 * @param images The list of image indices to create. These refer to filenames appfiles/image&ltid&gt.jpg
	 */
	public FFMPEGVideoTask(String creation, List<Integer> images) {
		_creation = creation;
		_images = images;
	}
	
	@Override
	protected Void call() throws Exception {
		//Get the length of the audio file
		AudioFileFormat file = AudioSystem.getAudioFileFormat(new File("appfiles/audio.wav"));
		float length = file.getFrameLength() / file.getFormat().getFrameRate();

		if (_images.size() > 2) {
			//The normal method. Works with 3+ images.
			//The command takes care of scaling images to a 960x540 yuv420p 25fps video, and draws the search term in appfiles/search-term.txt with a white border 3/4 of the way down the video.
			FFMPEGCommand video = new FFMPEGCommand(_images.stream().map(i -> "appfiles/image" + i + ".jpg").toArray(String[]::new), length / _images.size(), false, "creations/" + _creation + ".mp4", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25");

			if (!video.pipeFilesIn(() -> isCancelled())) {
				return null;
			}

			try {
				video.waitFor();
			} catch (Exception e) {
				//Try again with the old method
				video.useOldCommand();

				if (!video.pipeFilesIn(() -> isCancelled())) {
					video.pipeFilesIn(() -> isCancelled());
				}

				video.waitFor();
			}
		} else if (_images.size() > 0) {
			//FFMPEG and MediaPlayer do not work properly with 1-2 images with the other method, so it uses an alternative method.
			String[] strings = new String[2];

			if (_images.size() > 1) {
				strings[0] = "appfiles/image" + _images.get(0) + ".jpg";
				strings[1] = "appfiles/image" + _images.get(1) + ".jpg";
			} else {
				//1 is not enough, it needs 2
				strings[0] = "appfiles/image" + _images.get(0) + ".jpg";
				strings[1] = "appfiles/image" + _images.get(0) + ".jpg";
			}

			FFMPEGCommand video = new FFMPEGCommand(strings, length / 2, true, "creations/" + _creation + ".mp4", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25");

			if (!video.pipeFilesIn(() -> isCancelled())) {
				return null;
			}

			video.waitFor();

		} else {
			//Method for no images. Just renders a white background
			FFMPEGCommand video = new FFMPEGCommand(length, "creations/" + _creation + ".mp4", "-i", "appfiles/audio.wav", "-vf", "scale=w=min(iw*540/ih\\,960):h=min(540\\,ih*960/iw),pad=w=960:h=540:x=(960-iw)/2:y=(540-ih)/2,drawtext=textfile=appfiles/search-term.txt:x=(w-text_w)/2:y=(h*3/4-text_h/2):fontsize=72:borderw=2:bordercolor=white:expansion=none", "-pix_fmt", "yuv420p", "-r", "25");
			video.waitFor();
		}

		// generate thumbnail image
		if (_images.size() > 0) {
			try (InputStream input = new FileInputStream(new File("appfiles/image" + _images.get(0) + ".jpg")); FileOutputStream output = new FileOutputStream("creations/" + _creation + ".jpg")) {
				byte[] transfer = new byte[4096];
				int count;

				while ((count = input.read(transfer)) != -1) {
					if (isCancelled()) {
						return null;
					}

					output.write(transfer, 0, count);
				}
			}
		}

		return null;
	}
}
