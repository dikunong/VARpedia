package varpedia.controllers;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.tasks.PlayChunkTask;
import varpedia.tasks.VoiceListTask;

public class TextEditorController extends Controller {

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
    	
    	//TODO: Do this properly
    	new PlayChunkTask(wikiTextArea.getSelectedText(), null, null).run();
    	Task<String[]> dat = new VoiceListTask();
    	dat.run();
    	
    	try {
			System.out.println(Arrays.toString(dat.get()));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
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
        changeScene(event, "../ChunkAssemblerScreen.fxml");
    }

    @FXML
    private void pressCancelButton(ActionEvent event) {
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
