package varpedia.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import varpedia.models.Command;
import varpedia.models.FFMPEGCommand;

/**
 * Background task that handles the previewing/saving of audio chunks, using the festival voice synthesizer.
 *
 * @author Tudor Zagreanu
 */
public class PlayChunkTask extends Task<Void> {
	private String _inputText;
	private String _filename;
	private String _voice;
	
	/**
	 * @param input The text to speak/save
	 * @param filename The filename to save. Set to null to preview the text instead.
	 * @param voice The voice to use. Set to null to use the default.
	 */
	public PlayChunkTask(String input, String filename, String voice) {
		_inputText = input;
		_filename = filename;
		_voice = voice;
	}
	
	@Override
	protected Void call() throws Exception {
		if (_filename != null) {
			new File(_filename).mkdirs();
		}
		
		// put the scheme file in appfiles because festival won't go inside a jar
		try (FileOutputStream dest = new FileOutputStream(new File("appfiles/varpedia.scm")); InputStream in = PlayChunkTask.class.getResourceAsStream("/varpedia/varpedia.scm")) {
			byte[] transfer = new byte[4096];
			int count;
			
			while ((count = in.read(transfer)) != -1) {
				if (isCancelled()) {
					return null;
				}
				
				dest.write(transfer, 0, count);
			}
		}
		
		// remove accents
		String input = Normalizer.normalize(_inputText, Normalizer.Form.NFKD);
		input = input.replaceAll("[^\\p{ASCII}]", "");

		Command cmd;
		List<String> args = new ArrayList<String>();
		args.add("festival");
		args.add("--script");
		args.add("appfiles/varpedia.scm");
		
		if (_voice != null) {
			args.add("-voice");
			args.add(_voice);
		}
		
		String origFilename = _filename;
		boolean play = false;
		
		if (origFilename == null) {
			// on Linux the sound from the festival process keeps playing after the festival process is killed
			// so we'll make a fake audio chunk at a special location
			// the name means "Festival Usability on Linux"
			origFilename = "appfiles/audio-fulinux.dir";
			new File(origFilename).mkdirs();
			play = true;
		}
		
		args.add("-o");
		args.add(origFilename);
		
		cmd = new Command(args.toArray(new String[0]));
		cmd.run();
		cmd.getProcess().getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
		cmd.getProcess().getOutputStream().close();
		
		// wait for festival to finish
		try {
			if (cmd.getProcess().waitFor() != 0) {
				throw new Exception("Failed to create audio " + cmd.getProcess().exitValue());
			}
			
			// festival might error in this manner (return 0, with stderr output) when a voice is made to say something it doesn't want to say.
			String err = cmd.getError();
			
			if (err.contains("ERROR")) {
				throw new Exception("Failed to create audio because " + err);
			}
		} catch (InterruptedException e) {
			cmd.endForcibly();
			return null;
		}
		
		String filename = origFilename;
		
		if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }

		// now merge the chunks together
		List<String> files = Arrays.asList(new File(origFilename).list());
		
		// sort them numerically
		files.sort((String a, String b) -> {
			int aInt = Integer.parseInt(a.substring(0, a.lastIndexOf('.')));
			int bInt = Integer.parseInt(b.substring(0, b.lastIndexOf('.')));
			return Integer.compare(aInt, bInt);
		});
		
		final String reallyJavaFilename = origFilename;
		String[] fullFiles = files.stream().map((String a) -> reallyJavaFilename + "/" + a).toArray(String[]::new);
		
		// concatenate the subchunks (similar to how FFMPEGVideoTask makes audio
		FFMPEGCommand audio = new FFMPEGCommand(fullFiles, -1, false, filename + ".wav");

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
	
		// play the audio-fulinux creation if the task was meant to play audio
		if (play) {
			new PreviewAudioTask(new File(filename + ".wav")).run();
		}
		
		return null;
	}
}
