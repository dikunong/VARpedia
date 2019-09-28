package varpedia.controllers;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.tasks.FFMPEGVideoTask;
import varpedia.tasks.FlickrTask;

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

    private Task<? extends Object> _createTask;

    private ExecutorService pool = Executors.newCachedThreadPool();

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
    	//TODO: Far better method
    	if (_createTask == null) {
    		int imageCount = 10; //numOfImagesSpinner.getValue(); This doesn't seem to work properly
    		String name = creationNameTextField.getText();
    		
    		if (imageCount > 0 && imageCount <= 10) {
    			FlickrTask flickr = new FlickrTask("cat", imageCount);
    			_createTask = flickr;
    			_createTask.setOnSucceeded(ev -> {
                	try {
						_createTask = new FFMPEGVideoTask("cat", name, flickr.get(), Arrays.asList("Alarm01", "Alarm02", "Alarm03"));
	                	_createTask.setOnSucceeded(ev2 -> {
		                	System.out.println("Done");
		                    _createTask = null;
	                	});
	                	_createTask.setOnCancelled(ev2 -> {
	                        System.out.println("Cancel");
	                        _createTask = null;
	                    });
	                    _createTask.setOnFailed(ev2 -> {
	                        System.out.println("Fail");
	                        _createTask.getException().printStackTrace();
	                        _createTask = null;
	                    });
	                    pool.submit(_createTask);
                	} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                });
                _createTask.setOnCancelled(ev -> {
                    System.out.println("Cancel");
                    _createTask = null;
                });
                _createTask.setOnFailed(ev -> {
                    System.out.println("Fail");
                    _createTask.getException().printStackTrace();
                    _createTask = null;
                });	
            	pool.submit(_createTask);
        	} else {
        		//TODO: Error
        		System.out.println("No");
        	}
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
            changeScene(event, "/varpedia/MainScreen.fxml");
        }
    }

    @FXML
    private void pressBackButton(ActionEvent event) {
        changeScene(event, "/varpedia/TextEditorScreen.fxml");
    }
}
