package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;

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
