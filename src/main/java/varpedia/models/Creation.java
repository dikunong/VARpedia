package varpedia.models;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;

import javafx.scene.image.Image;

/**
 * Represents the information associated with a creation.
 * It can be serialized, in which case it is saved in a .dat file (creations/creationName.dat)
 *
 * @author Di Kun Ong and Tudor Zagreanu
 */
public class Creation implements Serializable {

	private static final long serialVersionUID = -7402469642522360829L;
	
	private final String _creationName;
	private final int _confidence;
	private final Instant _lastViewed;
	private transient Image _image;
	private transient boolean _initImage;

	/**
	 * @param name The filename of the creation, without the extension
	 * @param conf The confidence (1-5, or -1 for unrated)
	 * @param view The time the creation was last viewed (or null for unwatched)
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

	public Image getImage() {
		if (!_initImage) {
			File image = new File("creations/" + _creationName + ".jpg");
			
			if (image.exists()) {
				_image = new Image(image.toURI().toString());
			}
			
			_initImage = true;
		}
		
		return _image;
	}
}
