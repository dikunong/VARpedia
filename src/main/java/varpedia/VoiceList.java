package varpedia;

/**
 * Holds a list of voices and the default voice index.
 * 
 * @author PisuCat
 */
public class VoiceList {
	//Warthog, could do this in a better way.
	/**
	 * The default voice index, or -1 if it isn't there.
	 */
	public final int _defaultVoice;
	
	/**
	 * The list of voices.
	 */
	public Audio[] _voices;
	
	public VoiceList(String defaultVoice, Audio... voices) {
		_voices = voices;
		
		//Find the default voice
		for (int i = 0; i < voices.length; i++) {
			if (voices[i].getName().equals(defaultVoice)) {
				_defaultVoice = i;
				return;
			}
		}
		
		//Set to -1 if the default voice shows up nowhere
		_defaultVoice = -1;
	}
}
