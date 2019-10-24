package varpedia.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javafx.concurrent.Task;
import varpedia.Command;
import varpedia.FFMPEGCommand;

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
		//Concatenate audio files into a single audio file at appfiles/preaudio.wav or appfiles/audio.wav
		FFMPEGCommand audio = new FFMPEGCommand(_chunks.stream().map(s -> "appfiles/audio/" + s + ".wav").toArray(String[]::new), -1, false, _background == null ? "appfiles/audio.wav" : "appfiles/preaudio.wav");
		
		if (!audio.pipeFilesIn(() -> isCancelled())) {
			return null;
		}

		try {
			audio.waitFor();
		} catch (Exception e) {
			//Try again with the old method
			audio.useOldCommand();

			if (!audio.pipeFilesIn(() -> isCancelled())) {
				audio.pipeFilesIn(() -> isCancelled());
			}

			audio.waitFor();
		}
		
		//Now mix it with the music into appfiles/audio.wav
		if (_background != null) {
			//This command merges the preaudio.wav with the background music
			Command mixer = new Command("ffmpeg", "-y", "-i", "appfiles/preaudio.wav", "-i", "-", "-filter_complex", "[1]volume=volume=" + _volume + "[a];[0][a]amix=inputs=2:duration=first", "appfiles/audio.wav");
			mixer.run();
			
			//Pipe the audio in
			try (InputStream in = PlayChunkTask.class.getResourceAsStream(_background)) {
				byte[] transfer = new byte[4096];
				int count;
				
				//Copy the mp3 into the pipe
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
		
		return null;
	}
}
