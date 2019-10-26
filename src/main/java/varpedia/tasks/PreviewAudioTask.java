package varpedia.tasks;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import javafx.concurrent.Task;

/**
 * Plays an audio file using the Java AudioSystem.
 * 
 * @author Tudor Zagreanu
 */
public class PreviewAudioTask extends Task<Object> {
	private File _audio;
	
	/**
	 * Play an audio file using Java
	 * @param audio The audio file to play
	 */
	public PreviewAudioTask(File audio) {
		_audio = audio;
	}
	
	@Override
	protected Object call() throws Exception {
		AudioListener listener = new AudioListener();
		
		try (Clip clip = AudioSystem.getClip()) {
		    // add the listener to see when it is done
			clip.addLineListener(listener);
		    
		    // open the file in the clip
		    try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(_audio)) {
		    	clip.open(audioInputStream);
			}
			
		    // play and wait for it to finish
		    try {
		    	clip.start();
		    	listener.waitFor();
		    } finally {
		    	clip.close();
		    }
		} finally {
			// hopefully unstick any open files so they can be deleted
			System.gc();
		}
		
		return null;
	}

	public static class AudioListener implements LineListener {
		public volatile boolean done = false;
		
		@Override
		public synchronized void update(LineEvent event) {
			if (event.getType() == Type.STOP || event.getType() == Type.CLOSE) {
				done = true;
				notifyAll();
			}
		}
		
		// wait for the audio system to claim it is done
		public synchronized void waitFor() throws InterruptedException {
			while (!done) {
				wait();
			}
		}
	};

}
