package varpedia;

import java.io.Serializable;
import java.time.Instant;

public class Creation implements Serializable {

	private static final long serialVersionUID = -7402469642522360829L;
	
	private final String _creationName;
	private final int _confidence;
	private final Instant _lastViewed;

    public Creation(String name, int conf, Instant view) {
        _creationName = name;
        _confidence = conf;
        _lastViewed = view;
    }

    public String getCreationName() {
        return _creationName;
    }

    public int getConfidence() {
        return _confidence;
    }

    public Instant getLastViewed() {
        return _lastViewed;
    }
}
