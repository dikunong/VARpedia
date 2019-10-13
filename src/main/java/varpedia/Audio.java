package varpedia;

/**
 * Represents a bit of audio. Combines name and display
 * 
 * @author PisuCat
 */
public class Audio {
	private String _name;
	private String _display;
	
	/**
	 * @param name The name of the Audio
	 * @param display The display text of the Audio
	 */
	public Audio(String name, String display) {
		_name = name;
		_display = display;
	}
	
	/**
	 * @return The internal name of the audio
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @return The display name of the audio
	 */
	public String getDisplay() {
		return _display;
	}
	
	@Override
	public String toString() {
		return _display;
	}
}
