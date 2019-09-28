package varpedia.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.VARpediaApp;
import varpedia.tasks.FFMPEGVideoTask;
import varpedia.tasks.FlickrTask;
import varpedia.tasks.ListPopulateTask;

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

    @FXML
    private ObservableList<String> leftChunkList;
    @FXML
    private ListView<String> leftChunkListView;
    @FXML
    private ObservableList<String> rightChunkList;
    @FXML
    private ListView<String> rightChunkListView;

    private Task<? extends Object> _createTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private String term;

    @FXML
    private void initialize() {
        setLoadingInactive();
        numOfImagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
        numOfImagesSpinner.getValueFactory().setValue(10);
    	term = getDataFromFile("search-term.txt");

    	// populate list view with saved chunks
        populateList();
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

            System.out.println("Debug please");
            for (String s : rightChunkList) {
                System.out.println(s);
            }
    		
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

                			_createTask = new FFMPEGVideoTask(term, name, images, rightChunkList);
    	                	_createTask.setOnSucceeded(ev2 -> {
    		                    _createTask = null;
    		                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Created creation.");
    	                        alert.showAndWait();
    	                        setLoadingInactive();
    	                        changeScene(event, "/varpedia/MainScreen.fxml"); //TODO: Maybe go straight to player
    		                });
    	                	_createTask.setOnCancelled(ev2 -> {
    	                        _createTask = null;
    	                        setLoadingInactive();
    	                    });
    	                    _createTask.setOnFailed(ev2 -> {
    	                    	Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create creation.");
    	                        alert.showAndWait();
    	                        _createTask = null;
    	                        setLoadingInactive();
    	                    });
    	                    pool.submit(_createTask);
                		}
                	} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
                });
                _createTask.setOnCancelled(ev -> {
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnFailed(ev -> {
                	Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to download images.");
                    alert.showAndWait();
                    _createTask = null;
                    setLoadingInactive();
                });
            	pool.submit(_createTask);
            	setLoadingActive();
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
            if (_createTask != null && _createTask.isRunning()) {
                _createTask.cancel();
            }
            // open MainScreen
            changeScene(event, "/varpedia/MainScreen.fxml");
        }
    }

    @FXML
    private void pressBackButton(ActionEvent event) {
        changeScene(event, "/varpedia/TextEditorScreen.fxml");
    }

    @FXML
    private void pressAddToButton(ActionEvent event) {
        String selectedChunk = leftChunkListView.getSelectionModel().getSelectedItem();
        // check something is selected in leftChunkList
        if (selectedChunk != null) {
            // add it to rightChunkList
            rightChunkList.add(selectedChunk);
            // remove it from leftChunkList
            leftChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressRemoveFromButton(ActionEvent event) {
        String selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        // check something is selected in rightChunkList
        if (selectedChunk != null) {
            // add it to leftChunkList
            leftChunkList.add(selectedChunk);
            // remove it from rightChunkList
            rightChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressMoveUpButton(ActionEvent event) {
        String selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        int selectedIndex = rightChunkListView.getSelectionModel().getSelectedIndex();
        // check something is selected in rightChunkList and it's not already first
        if (selectedChunk != null && selectedIndex > 0) {
            // change its index if it's not already first
            rightChunkList.remove(selectedIndex);
            rightChunkList.add(selectedIndex - 1, selectedChunk);
            rightChunkListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    private void pressMoveDownButton(ActionEvent event) {
        String selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        int selectedIndex = rightChunkListView.getSelectionModel().getSelectedIndex();
        int maxIndex = rightChunkListView.getItems().size() - 1;
        // check something is selected in rightChunkList and it's not already last
        if (selectedChunk != null && selectedIndex < maxIndex) {
            rightChunkList.remove(selectedIndex);
            rightChunkList.add(selectedIndex + 1, selectedChunk);
            rightChunkListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    private void populateList() {
        Task<List<String>> task = new ListPopulateTask(new File("appfiles/audio"));
        task.setOnSucceeded(event -> {
            try {
                List<String> newCreations = task.get();
                if (newCreations != null) {
                    leftChunkList.addAll(newCreations);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        pool.submit(task);
    }

    private void setLoadingActive() {
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        creationNameTextField.setDisable(true);
        numOfImagesSpinner.setDisable(true);
        //createBtn.setDisable(true);
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
        //createBtn.setDisable(false);
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
