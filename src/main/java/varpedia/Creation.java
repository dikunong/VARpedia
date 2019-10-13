package varpedia;

import java.io.Serializable;
import java.time.Instant;

/**
 * This object represents the information associated with a creation
 * It is saved in a .dat file (creations/&ltcreationName&gt.dat)
 * @author PisuCat
 */
public class Creation implements Serializable {

	private static final long serialVersionUID = -7402469642522360829L;
	
	private final String _creationName;
	private final int _confidence;
	private final Instant _lastViewed;

	/**
	 * @param name The filename of the creation without the extension
	 * @param conf The confidence (1-5, or -1 for unrated)
	 * @param view The last view time (or null for unwatched)
	 */
    public Creation(String name, int conf, Instant view) {
        _creationName = name;
        _confidence = conf;
        _lastViewed = view;
    }

    /**
     * @return The filename of the creation without the extension
     */
    public String getCreationName() {
        return _creationName;
    }

    /**
     * @return The confidence (1-5, or -1 for unrated)
     */
    public int getConfidence() {
        return _confidence;
    }

    /**
     * @return The last view time (or null for unwatched)
     */
    public Instant getLastViewed() {
        return _lastViewed;
    }
}
