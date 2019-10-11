package varpedia;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;

public class Creation implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7402469642522360829L;
	
	private transient SimpleStringProperty _creationName = new SimpleStringProperty("");
    private transient SimpleIntegerProperty _confidence = new SimpleIntegerProperty(0);
    private transient SimpleObjectProperty<Instant> _lastViewed = new SimpleObjectProperty<Instant>(null);

    public Creation(String name, int conf, Instant view) {
        _creationName.set(name);
        _confidence.set(conf);
        _lastViewed.set(view);
    }

    public String getCreationName() {
        return _creationName.get();
    }

    public StringProperty creationNameProperty() {
        return _creationName;
    }

    public Integer getConfidence() {
        return _confidence.get();
    }

    public IntegerProperty confidenceProperty() {
        return _confidence;
    }

    public Instant getLastViewed() {
        return _lastViewed.get();
    }

    public ObjectProperty<Instant> lastViewedProperty() {
        return _lastViewed;
    }
    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(_creationName.get());
        stream.writeInt(_confidence.get());
        stream.writeObject(_lastViewed.get());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    	_creationName = new SimpleStringProperty(stream.readUTF());
        _confidence = new SimpleIntegerProperty(stream.readInt());
        _lastViewed = new SimpleObjectProperty<Instant>((Instant)stream.readObject());
    }
}
