package varpedia.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.tasks.FFMPEGVideoTask;

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
    private Button backBtn;

    private Task<Void> _createTask;
    
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
    	//TODO: Better method
    	if (_createTask == null) {
        	_createTask = new FFMPEGVideoTask("cat", "puskin", 10, new String[] {"Alarm01", "Alarm02", "Alarm03"});
        	Thread thread = new Thread(_createTask);
            thread.start();

            _createTask.setOnSucceeded(ev -> {
                System.out.println("Done");
                _createTask = null;
            });
            
            _createTask.setOnCancelled(ev -> {
                System.out.println("Cancel");
                _createTask = null;
            });	
    	} else {
    		_createTask.cancel(true);
    	}
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

    @FXML
    private void pressBackButton(ActionEvent event) {
        changeScene(event, "../TextEditorScreen.fxml");
    }
}
