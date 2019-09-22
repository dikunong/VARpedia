package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChunkAssemblerController extends Controller {

    @FXML
    private Button addToBtn;
    @FXML
    private Button removeFromBtn;
    @FXML
    private Button moveUpBtn;
    @FXML
    private Button moveDownBtn;
    @FXML
    private TextField creationNameTextField;
    @FXML
    private Spinner<Integer> numOfImagesSpinner;
    @FXML
    private Button createBtn;
    @FXML
    private Button cancelBtn;

    @FXML
    private void initialize() {
        // stuff
    }

    @FXML
    private void pressCreateBtn(ActionEvent event) {
        // open CreationProgressScreen - or should this be a dialog window?
        // assemble audio chunks
        // get Flickr images
        // assemble audio + video using ffmpeg
        // does this stuff happen here or in CreationProgressScreen?
    }

    @FXML
    private void pressCancelBtn(ActionEvent event) {
        // ask for confirmation first!
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel making " +
                "the current creation?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            // discard all existing temp files etc
            changeScene(event, "../MainScreen.fxml");
        }
    }
}
