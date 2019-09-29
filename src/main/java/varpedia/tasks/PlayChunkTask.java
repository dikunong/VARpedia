package varpedia.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import varpedia.Command;

public class PlayChunkTask extends Task<Void> {
	private String _inputText;
	private String _filename;
	private String _voice;
	
	/**
	 * @param input The text to speak/save
	 * @param filename The filename to save. Set to null to play the text out the speaker instead.
	 * @param voice The voice to use. Set to null to use the default.
	 */
	public PlayChunkTask(String input, String filename, String voice) {
		_inputText = input;
		_filename = filename;
		_voice = voice;
	}
	
	@Override
	protected Void call() throws Exception {
		//Put the scheme file in appfiles because festival won't go inside a jar.
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
		
		//Remove accents
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
		
		if (_filename != null) {
			args.add("-o");
			args.add(_filename);
		}
		
		cmd = new Command(args.toArray(new String[0]));
		cmd.run();
		cmd.getProcess().getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
		cmd.getProcess().getOutputStream().close();
		
		//Wait for festival to finish
		try {
			if (cmd.getProcess().waitFor() != 0) {
				throw new Exception("Failed to create audio " + cmd.getProcess().exitValue());
			}
			
			//Festival might error in this manner (return 0, with stderr output) when a voice is made to say something it doesn't want to say.
			String err = cmd.getError();
			
			if (err.contains("ERROR")) {
				throw new Exception("Failed to create audio because " + err);
			}
		} catch (InterruptedException e) {
			cmd.end();
		}		
		
		return null;
	}
}
