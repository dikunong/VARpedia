package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.AlertHelper;

import java.util.function.UnaryOperator;

public class RatingController extends Controller {

    @FXML
    private Slider ratingSlider;
    @FXML
    private Spinner<Integer> ratingSpinner;
    @FXML
    private Button saveBtn;
    @FXML
    private Button dontSaveBtn;

    private AlertHelper _alertHelper = AlertHelper.getInstance();

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
    }

    @FXML
    private void pressSaveBtn(ActionEvent event) {
        // save rating

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
