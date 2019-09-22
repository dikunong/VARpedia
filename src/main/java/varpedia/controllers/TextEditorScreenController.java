package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class TextEditorScreenController extends Controller {

    @FXML
    private TextArea wikiTextArea;
    @FXML
    private Button previewBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button assembleBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private ChoiceBox<String> voiceChoiceBox;

    @FXML
    private void initialize() {
        // stuff
    }

    @FXML
    private void pressPreviewButton(ActionEvent event) {
        // get selected text from wikiTextArea
        // play back selected text in Festival using selected Voice
    }

    @FXML
    private void pressSaveButton(ActionEvent event) {
        // get selected text from wikiTextArea
        // save selected text audio into .wav "chunk"
        // what should these files be named?
    }

    @FXML
    private void pressAssembleButton(ActionEvent event) {
        // open ChunkAssemblerScreen
    }

    @FXML
    private void pressCancelButton(ActionEvent event) {
        // ask for confirmation first!
        // discard all existing temp files etc
        changeScene(event, "../MainScreen.fxml");
    }

}
