package varpedia;

import java.io.Serializable;

/**
 * Represents a piece of audio - could be a chunk, a voice, or background music.
 * It can be serialized, in which case it is saved in a .dat file (appfiles/audio/chunkName.dat)
 * 
 * @author Di Kun Ong and Tudor Zagreanu
 */
public class Audio implements Serializable {

	private static final long serialVersionUID = -8659820389864968350L;

	private String _name;
	private String _display;
	
	/**
	 * @param name The name of the Audio - typically a filename
	 * @param display The display text of the Audio - typically a user-friendly representation
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
