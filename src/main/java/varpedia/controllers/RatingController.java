package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.AlertHelper;
import varpedia.Creation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.function.UnaryOperator;

public class RatingController extends Controller {

    @FXML
    private Slider ratingSlider;
    @FXML
    private Spinner<Integer> ratingSpinner;
    @FXML
    private Button saveRateBtn;
    @FXML
    private Button dontSaveRateBtn;

    private AlertHelper _alertHelper = AlertHelper.getInstance();

    private String _creationName;
    
    @FXML
    private void initialize() {
        // give the numOfImagesSpinner a range of 0-10
        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5));

        // set numOfImagesSpinner TextFormatter to only accept integers
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[1-5]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> intFormatter = new TextFormatter<>(filter);
        ratingSpinner.getEditor().setTextFormatter(intFormatter);

        // make numOfImagesSpinner listen for typed input
        ratingSpinner.getValueFactory().setValue(0);
        ratingSpinner.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                ratingSpinner.increment(0);
            }
        }));
        
        _creationName = getDataFromFile("playback-name.txt");
    }

    @FXML
    private void pressSaveBtn(ActionEvent event) {
        // save rating
    	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("creations/" + _creationName + ".dat")))) {
    		oos.writeObject(new Creation(_creationName, ratingSpinner.getValue(), Instant.now()));
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
        closeDialog(event);
    }

    @FXML
    private void pressDontSaveBtn(ActionEvent event) {
        // ask for confirmation first
        _alertHelper.showAlert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to continue without rating?",
                ButtonType.YES, ButtonType.CANCEL);
        if (_alertHelper.getResult() == ButtonType.YES) {
            closeDialog(event);
        }
    }
}
