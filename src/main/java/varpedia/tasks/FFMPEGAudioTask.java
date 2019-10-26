package varpedia.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javafx.concurrent.Task;
import varpedia.models.Command;
import varpedia.models.FFMPEGCommand;

/**
 * Background task that handles all FFMPEG commands for creation audio creation - including concatenation of audio chunks.
 *
 * @author Tudor Zagreanu
 */
public class FFMPEGAudioTask extends Task<Void> {
	
	private List<String> _chunks;
	private String _background;
	private double _volume;
	
	/**
	 * @param chunks The list of chunk names. These refer to filenames appfiles/audio/&ltchunk name&gt.wav
	 * @param background The background music in a file (or null if there is no background music)
	 * @param volume The volume of the background music
	 */
	public FFMPEGAudioTask(List<String> chunks, String background, double volume) {
		_chunks = chunks;
		_background = background;
		_volume = volume;
	}
	
	@Override
	protected Void call() throws Exception {
		// concatenate audio files into a single audio file at appfiles/preaudio.wav or appfiles/audio.wav
		FFMPEGCommand audio = new FFMPEGCommand(_chunks.stream().map(s -> "appfiles/audio/" + s + ".wav").toArray(String[]::new), -1, false, _background == null ? "appfiles/audio.wav" : "appfiles/preaudio.wav");
		
		if (!audio.pipeFilesIn(() -> isCancelled())) {
			return null;
		}

		try {
			audio.waitFor();
		} catch (Exception e) {
			// try again with the old method
			audio.useOldCommand();

			if (!audio.pipeFilesIn(() -> isCancelled())) {
				audio.pipeFilesIn(() -> isCancelled());
			}

			audio.waitFor();
		}
		
		// now mix it with the music into appfiles/audio.wav
		if (_background != null) {
			// this command merges the preaudio.wav with the background music
			Command mixer = new Command("ffmpeg", "-y", "-i", "appfiles/preaudio.wav", "-i", "-", "-filter_complex", "[1]volume=volume=" + _volume + "[a];[0][a]amix=inputs=2:duration=first", "appfiles/audio.wav");
			mixer.run();
			
			// pipe the audio in
			try (InputStream in = PlayChunkTask.class.getResourceAsStream(_background)) {
				byte[] transfer = new byte[4096];
				int count;
				
				// copy the mp3 into the pipe
				while ((count = in.read(transfer)) != -1) {
					if (isCancelled()) {
						return null;
					}
					
					try {
						mixer.getProcess().getOutputStream().write(transfer, 0, count);
					} catch (IOException e) {
						// ffmpeg doesn't bother reading the whole pipe, and will close the pipe, so just exit the loop
						break;
					}
				}
			}
			
			try {
				mixer.getProcess().getOutputStream().close();
			} catch (IOException e) {
				// this need to prevent "The pipe is being closed"
			}
				
			new Thread(() -> {mixer.getError();}).start(); // ffmpeg needs its stderr to be emptied
			
			// wait for it to be done
			try {
				if (mixer.getProcess().waitFor() != 0) {
					throw new Exception("Failed to mix audio " + mixer.getProcess().exitValue());
				}
			} catch (InterruptedException e) {
				mixer.end();
				return null;
			}
		}
		
		return null;
	}
}
