package varpedia.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.VARpediaApp;
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

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private String term;

    @FXML
    private void initialize() {
    	term = getDataFromFile("search-term.txt");
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
    		
    		if (name == null || name.isEmpty()) {
    			Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a creation name.");
                alert.showAndWait();
    		} else if (imageCount <= 0 || imageCount > 10) {
    			Alert alert = new Alert(Alert.AlertType.ERROR, "You must select between 1 and 10 images (inclusive).");
                alert.showAndWait();
    		} else {
    			FlickrTask flickr = new FlickrTask(term, imageCount);
    			_createTask = flickr;
    			_createTask.setOnSucceeded(ev -> {
                	try {
                		int actualImages = flickr.get();
                		boolean actual = false;
                		
                		if (actualImages < imageCount) {
                			Alert alert = new Alert(Alert.AlertType.WARNING, "Fewer images were retrieved than requested (" + actualImages + "). Continue anyway?", ButtonType.YES, ButtonType.CANCEL);
            	            alert.showAndWait();
            	            
            	            if (alert.getResult() == ButtonType.YES) {
            	            	actual = true;
            	            }
                		} else {
                			actual = true;
                		}
                		
                		if (actual) {
                			List<Integer> images = new ArrayList<Integer>();
                			
                			for (int i = 0; i < actualImages; i++) {
                				images.add(i);
                			}
                			
                			_createTask = new FFMPEGVideoTask(term, name, images, Arrays.asList("Alarm01", "Alarm02", "Alarm03"));
    	                	_createTask.setOnSucceeded(ev2 -> {
    		                    _createTask = null;
    		                    Alert alert = new Alert(Alert.AlertType.ERROR, "Created creation.");
    	                        alert.showAndWait();
    	                        changeScene(event, "/varpedia/MainScreen.fxml"); //TODO: Maybe go straight to player
    		                });
    	                	_createTask.setOnCancelled(ev2 -> {
    	                        _createTask = null;
    	                    });
    	                    _createTask.setOnFailed(ev2 -> {
    	                    	Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create creation.");
    	                        alert.showAndWait();
    	                        _createTask = null;
    	                    });
    	                    pool.submit(_createTask);	
                		}
                	} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
                });
                _createTask.setOnCancelled(ev -> {
                    _createTask = null;
                });
                _createTask.setOnFailed(ev -> {
                	Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to download images.");
                    alert.showAndWait();
                    _createTask = null;
                });	
            	pool.submit(_createTask);
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
