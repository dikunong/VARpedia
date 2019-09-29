package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;

/**
 * Background task that handles retrieving a list of all installed festival voices available on the user's system.
 *
 * Author: Tudor Zagreanu
 */
public class VoiceListTask extends Task<String[]> {
	@Override
	protected String[] call() throws Exception {
		Command cmd = new Command("festival", "(print (voice.list))", "(exit)");
		cmd.run();
		String output = cmd.getOutput();
		
		//Festival outputs "(<voice1> <voice2> <voice3>)"
		return output.substring(1, output.length() - 2).split(" ");
	}
}
