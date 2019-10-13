package varpedia.tasks;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import javafx.concurrent.Task;

public class PreviewAudioTask extends Task<Object> {
	private File _audio;
	
	public PreviewAudioTask(File audio) {
		_audio = audio;
	}
	
	@Override
	protected Object call() throws Exception {
		AudioListener listener = new AudioListener();
		
		try (Clip clip = AudioSystem.getClip()) {
		    clip.addLineListener(listener);
		    
		    try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(_audio)) {
		    	clip.open(audioInputStream);
			}
			
		    try {
		    	clip.start();
		    	listener.waitFor();
		    } finally {
		    	clip.close();
		    }
		} finally {
			System.gc();
		}
		
		return null;
	}
	
	public static class AudioListener implements LineListener {
		public volatile boolean done = false;
		
		@Override
		public synchronized void update(LineEvent event) {
			if (event.getType() == Type.STOP || event.getType() == Type.STOP) {
				done = true;
				notifyAll();
			}
		}
		
		public synchronized void waitFor() throws InterruptedException {
			while (!done) {
				wait();
			}
		}
	};

}
