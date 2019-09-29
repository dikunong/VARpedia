package varpedia.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.UnaryOperator;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
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

    @FXML
    private void initialize() {
        setLoadingInactive();

        // give the numOfImagesSpinner a range of 0-10
        numOfImagesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));

        // set numOfImagesSpinner TextFormatter to only accept integers
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> intFormatter = new TextFormatter<>(filter);
        numOfImagesSpinner.getEditor().setTextFormatter(intFormatter);

        // make numOfImagesSpinner listen for typed input
        numOfImagesSpinner.getValueFactory().setValue(10);
        numOfImagesSpinner.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                numOfImagesSpinner.increment(0);
            }
        }));

    	// populate list view with saved chunks
        populateList();
    }

    @FXML
    private void pressCreateBtn(ActionEvent event) {
        if (_createTask == null) {
    		int imageCount = numOfImagesSpinner.getValueFactory().getValue();
    		String name = creationNameTextField.getText();
    		
    		if (name == null || name.isEmpty()) {
    			Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a creation name.");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
    		} else if (!name.matches("[-_. A-Za-z0-9]+")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid creation name (only letters, numbers, spaces, -, _).");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
            } else if (imageCount <= 0 || imageCount > 10) {
    			Alert alert = new Alert(Alert.AlertType.ERROR, "You must select between 1 and 10 images (inclusive).");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    			alert.showAndWait();
    		} else if (rightChunkListView.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select chunks to assemble.");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
    		} else {

    		    // check if creation already exists and offer option to overwrite
                // this must go here in order to allow application flow to continue if user chooses to overwrite
    		    if (checkDuplicate(name)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Creation already exists. Overwrtie?", ButtonType.YES, ButtonType.CANCEL);
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.CANCEL) {
                        return;
                    }
                }

    			// get Flickr images
    	        FlickrTask flickr = new FlickrTask(imageCount);
    			_createTask = flickr;
    			_createTask.setOnSucceeded(ev -> {
                	try {
                		int actualImages = flickr.get();
                		boolean actual = false;

                		if (actualImages < imageCount) {
                			Alert alert = new Alert(Alert.AlertType.WARNING, "Fewer images were retrieved than requested (" + actualImages + "). Continue anyway?", ButtonType.YES, ButtonType.CANCEL);
                            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                			alert.showAndWait();

            	            if (alert.getResult() == ButtonType.YES) {
            	            	actual = true;
            	            }
                		} else {
                			actual = true;
                		}

                		if (actual) {
                			//assemble images
                			List<Integer> images = new ArrayList<Integer>();

                			for (int i = 0; i < actualImages; i++) {
                				images.add(i);
                			}

                			// assemble audio + video using ffmpeg
                	    	_createTask = new FFMPEGVideoTask(name, images, rightChunkList);
    	                	_createTask.setOnSucceeded(ev2 -> {
    		                    _createTask = null;
    		                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Created creation.");
                                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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
                                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            // if a creation is currently in progress, cancel it
            if (_createTask != null && _createTask.isRunning()) {
                _createTask.cancel();
            }
            // open MainScreen
            changeScene(event, "/varpedia/MainScreen.fxml");
        }
    }

    @FXML
    private void pressBackButton(ActionEvent event) {
        // open TextEditorScreen - do not lose any progress
        changeScene(event, "/varpedia/TextEditorScreen.fxml");
    }

    @FXML
    private void pressAddToButton(ActionEvent event) {
        String selectedChunk = leftChunkListView.getSelectionModel().getSelectedItem();
        // if something is selected in leftChunkList, shift it to rightChunkList
        if (selectedChunk != null) {
            rightChunkList.add(selectedChunk);
            leftChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressRemoveFromButton(ActionEvent event) {
        String selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        // if something is selected in rightChunkList, shift it to leftChunkList
        if (selectedChunk != null) {
            leftChunkList.add(selectedChunk);
            rightChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressMoveUpButton(ActionEvent event) {
        String selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        int selectedIndex = rightChunkListView.getSelectionModel().getSelectedIndex();
        // if something is selected in rightChunkList and it's not already first, shift its index by -1
        if (selectedChunk != null && selectedIndex > 0) {
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
        // if something is selected in rightChunkList and it's not already last, shift its index by +1
        if (selectedChunk != null && selectedIndex < maxIndex) {
            rightChunkList.remove(selectedIndex);
            rightChunkList.add(selectedIndex + 1, selectedChunk);
            rightChunkListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    /**
     * Helper method that determines if a creation already exists.
     * @param name Creation being checked for duplicate status
     * @return true if creation exists
     */
    private boolean checkDuplicate(String name) {
        File file = new File("creations/" + name + ".mp4");
        return file.exists();
    }

    /**
     * Helper method that runs a task to populate the leftChunkList with chunks in the appfiles/audio directory.
     */
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

    /**
     * Helper method to disable most UI elements and show loading indicators while a creation task is in progress.
     */
    private void setLoadingActive() {
        createBtn.setText("Stop");
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        creationNameTextField.setDisable(true);
        numOfImagesSpinner.setDisable(true);
        backBtn.setDisable(true);
        loadingBar.setVisible(true);
        loadingLabel.setVisible(true);
    }

    /**
     * Helper method to enable most UI elements and hide loading indicators when a creation task ends.
     */
    private void setLoadingInactive() {
        createBtn.setText("Create!");
        addToBtn.setDisable(false);
        removeFromBtn.setDisable(false);
        moveUpBtn.setDisable(false);
        moveDownBtn.setDisable(false);
        creationNameTextField.setDisable(false);
        numOfImagesSpinner.setDisable(false);
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
