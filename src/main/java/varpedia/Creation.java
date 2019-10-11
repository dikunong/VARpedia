package varpedia;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

public class Creation implements Serializable {

    private SimpleStringProperty _creationName = new SimpleStringProperty("");
    private SimpleStringProperty _confidence = new SimpleStringProperty("");
    private SimpleStringProperty _lastViewed = new SimpleStringProperty("");

    public Creation(String name, String conf, String view) {
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

    public String getConfidence() {
        return _confidence.get();
    }

    public StringProperty confidenceProperty() {
        return _confidence;
    }

    public String getLastViewed() {
        return _lastViewed.get();
    }

    public StringProperty lastViewedProperty() {
        return _lastViewed;
    }
}
