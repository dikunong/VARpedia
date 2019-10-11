package varpedia.controllers;

import java.io.File;
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
import varpedia.AlertHelper;
import varpedia.VARpediaApp;
import varpedia.tasks.FlickrTask;
import varpedia.tasks.ListPopulateTask;

/**
 * Controller for the ChunkAssemblerScreen, which handles assembly of audio chunks, input of creation name and number
 * of images, and the actual creation of the creation itself via Flickr and FFMPEG.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
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

    private Task<Integer> _createTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    private AlertHelper _alertHelper = AlertHelper.getInstance();

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
    		int imageCount = 10;
    		
    		if (rightChunkListView.getItems().isEmpty()) {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Please add chunks to assemble.");
    		} else {
    			// get Flickr images
    			_createTask = new FlickrTask(imageCount);
    			_createTask.setOnSucceeded(ev -> {
                	try {
                		int actualImages = _createTask.get();
                		sendDataToFile(Integer.toString(actualImages), "image-count.txt");
                		StringBuilder selected = new StringBuilder();

                		for (String s : rightChunkList) {
                			selected.append(s);
                			selected.append(File.pathSeparator);
                		}

                		sendDataToFile(selected.toString(), "selected-chunks.txt");
                		setLoadingInactive();
                		changeScene(event, "/varpedia/PhotoPickerScreen.fxml");
                	} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
                });
                _createTask.setOnCancelled(ev -> {
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnFailed(ev -> {
                    _alertHelper.showAlert(Alert.AlertType.ERROR, "Failed to download images.");
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
        _alertHelper.showAlert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to cancel making the current creation?",
                ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
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
