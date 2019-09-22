package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

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
        // discard all existing temp files etc
        changeScene(event, "../MainScreen.fxml");
    }
}
