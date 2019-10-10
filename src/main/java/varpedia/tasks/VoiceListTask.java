package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;
import varpedia.VoiceList;

/**
 * Background task that handles retrieving a list of all installed festival voices available on the user's system.
 *
 * Author: Tudor Zagreanu
 */
public class VoiceListTask extends Task<VoiceList> {
	@Override
	protected VoiceList call() throws Exception {
		Command cmd = new Command("festival", "(print (voice.list))", "(print (list voice_default))", "(exit)");
		cmd.run();
		String output = cmd.getOutput();
		
		//Festival outputs "(<voice1> <voice2> <voice3>)\n(voice_<default>)\n"
		String[] lines = output.split("\n");
		String[] voices = lines[0].substring(1, lines[0].length() - 1).split(" ");
		String defaultVoice = lines[1].substring(7, lines[1].length() - 1);
		
		return new VoiceList(defaultVoice, voices);
	}
}
