package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Audio;
import varpedia.Command;
import varpedia.VoiceList;

/**
 * Background task that handles retrieving a list of all installed festival voices available on the user's system.
 *
 * @author Tudor Zagreanu
 */
public class VoiceListTask extends Task<VoiceList> {
	//Returns the display name for the input. Currently hardcoded.
	//Yes, the default festival voice is called Kevin.
	private static String getDisplayName(String name) {
		if (name.equals("kal_diphone")) {
			return "Kevin (Male US)";
		} else if (name.equals("akl_nz_jdt_diphone")) {
			return "Male NZ";
		} else if (name.equals("akl_nz_cw_cg_cg")) {
			return "Female NZ";
		} else {
			return name;
		}
	}
	
	@Override
	protected VoiceList call() throws Exception {
		Command cmd = new Command("festival", "(print (voice.list))", "(print (list voice_default))", "(exit)");
		cmd.run();
		String output = cmd.getOutput();
		
		//Festival outputs "(<voice1> <voice2> <voice3>)\n(voice_<default>)\n"
		String[] lines = output.split("\n");
		String[] voices = lines[0].substring(1, lines[0].length() - 1).split(" ");
		String defaultVoice = lines[1].substring(7, lines[1].length() - 1);
		Audio[] voiceObjs = new Audio[voices.length];
		
		for (int i = 0; i < voices.length; i++) {
			voiceObjs[i] = new Audio(voices[i], getDisplayName(voices[i]));
		}
		
		return new VoiceList(defaultVoice, voiceObjs);
	}
}
