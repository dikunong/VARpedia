package varpedia.tasks;

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
		Command cmd;
		
		if (_voice == null) {
			if (_filename == null) {
				cmd = new Command("festival", "(tts_file \"-\")", "(exit)");
			} else {
				cmd = new Command("text2wave", "-o", _filename);
			}
		} else {
			if (_filename == null) {
				cmd = new Command("festival", "(voice_" + _voice + ")", "(tts_file \"-\")", "(exit)");
			} else {
				cmd = new Command("text2wave", "--eval", "(voice_" + _voice + ")", "-o", _filename);
			}
		}
		
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
