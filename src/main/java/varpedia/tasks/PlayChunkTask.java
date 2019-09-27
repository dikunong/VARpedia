package varpedia.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import varpedia.Command;

public class PlayChunkTask extends Task<Void> {
	private String _inputText;
	private String _filename;
	private String _voice;
	
	public PlayChunkTask(String input, String filename, String voice) {
		_inputText = input;
		_filename = filename;
		_voice = voice;
	}
	
	@Override
	protected Void call() throws Exception {
		//TODO: Probably okay to put in main?
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
		cmd.getProcess().getOutputStream().write(_inputText.getBytes());
		cmd.getProcess().getOutputStream().close();
		
		if (cmd.getProcess().waitFor() != 0) {
			throw new Exception("Failed to create audio");
		}
		
		return null;		
		
		//Play audio: input text | festival '(voice_<voice>)' '(tts_file "-")' '(exit)'
		//Save audio: input text | text2wave --eval '(voice_<voice>)' -o <output>
		//Play audio: input text | festival '(tts_file "-")' '(exit)'
		//Save audio: input text | text2wave -o <output>
	}
}
