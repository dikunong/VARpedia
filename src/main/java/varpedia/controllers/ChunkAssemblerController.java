package varpedia.controllers;

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
    @FXML
    private ProgressBar loadingBar;
    @FXML
    private Label loadingLabel;

    private Task<? extends Object> _createTask;

    private ExecutorService pool = Executors.newCachedThreadPool();

    @FXML
    private void initialize() {
        setLoadingInactive();
        numOfImagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
        numOfImagesSpinner.getValueFactory().setValue(10);
    }

    @FXML
    private void pressCreateBtn(ActionEvent event) {
        // assemble audio chunks
        // get Flickr images
        // assemble audio + video using ffmpeg
    	//TODO: Far better method
    	if (_createTask == null) {
    		int imageCount = numOfImagesSpinner.getValueFactory().getValue();
    		String name = creationNameTextField.getText();
    		
    		if (imageCount > 0 && imageCount <= 10) {
    			FlickrTask flickr = new FlickrTask("cat", imageCount);
    			_createTask = flickr;
    			_createTask.setOnSucceeded(ev -> {
                	try {
						_createTask = new FFMPEGVideoTask("cat", name, flickr.get(), new String[] {"Alarm01", "Alarm02", "Alarm03"});
	                	_createTask.setOnSucceeded(ev2 -> {
		                	System.out.println("Done");
		                    _createTask = null;
                            setLoadingInactive();
	                	});
	                	_createTask.setOnCancelled(ev2 -> {
	                        System.out.println("Cancel");
	                        _createTask = null;
                            setLoadingInactive();
	                    });
	                    _createTask.setOnFailed(ev2 -> {
	                        System.out.println("Fail");
	                        _createTask.getException().printStackTrace();
	                        _createTask = null;
                            setLoadingInactive();
	                    });
	                    pool.submit(_createTask);
                        setLoadingActive();
                	} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                });
                _createTask.setOnCancelled(ev -> {
                    System.out.println("Cancel");
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnFailed(ev -> {
                    System.out.println("Fail");
                    _createTask.getException().printStackTrace();
                    _createTask = null;
                    setLoadingInactive();
                });	
            	pool.submit(_createTask);
            	setLoadingActive();
        	} else {
        		//TODO: Error
        		System.out.println("No");
        	}
    	} else {
    		_createTask.cancel(true);
    		setLoadingInactive();
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

    private void setLoadingActive() {
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        creationNameTextField.setDisable(true);
        numOfImagesSpinner.setDisable(true);
        createBtn.setDisable(true);
        backBtn.setDisable(true);
        loadingBar.setVisible(true);
        loadingLabel.setVisible(true);
    }

    private void setLoadingInactive() {
        addToBtn.setDisable(false);
        removeFromBtn.setDisable(false);
        moveUpBtn.setDisable(false);
        moveDownBtn.setDisable(false);
        creationNameTextField.setDisable(false);
        numOfImagesSpinner.setDisable(false);
        createBtn.setDisable(false);
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
