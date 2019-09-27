package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;

public class VoiceListTask extends Task<String[]> {
	@Override
	protected String[] call() throws Exception {
		Command cmd = new Command("festival", "(print (voice.list))", "(exit)");
		cmd.run();
		String output = cmd.getOutput();
		return output.substring(1, output.length() - 1).split(" ");
	}
}
