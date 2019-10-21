package varpedia.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.AlertHelper;
import varpedia.Audio;
import varpedia.VARpediaApp;
import varpedia.tasks.FlickrTask;
import varpedia.tasks.ListPopulateTask;

/**
 * Controller for the ChunkAssemblerScreen, which handles assembly of audio chunks,
 * and the downloading of images via Flickr.
 *
 * @author Di Kun Ong and Tudor Zagreanu
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
    private Button selectBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button backBtn;
    @FXML
    private ProgressBar loadingBar;
    @FXML
    private Label loadingLabel;

    @FXML
    private ObservableList<Audio> leftChunkList;
    @FXML
    private ListView<Audio> leftChunkListView;
    @FXML
    private ObservableList<Audio> rightChunkList;
    @FXML
    private ListView<Audio> rightChunkListView;

    private Task<Integer> _photoTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
        setLoadingInactive();

    	// populate list view with saved chunks
        populateList();

        // disable chunk lists if they are empty
        leftChunkListView.disableProperty().bind(Bindings.size(leftChunkList).isEqualTo(0));
        rightChunkListView.disableProperty().bind(Bindings.size(rightChunkList).isEqualTo(0));
    }

    @FXML
    private void pressSelectBtn(ActionEvent event) {
        if (_photoTask == null) {
    		int imageCount = 10;
    		
    		if (rightChunkListView.getItems().isEmpty()) {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Please add chunks to assemble.");
    		} else {
    			// get Flickr images
    			_photoTask = new FlickrTask(imageCount);
    			_photoTask.setOnSucceeded(ev -> {
                	try {
                		int actualImages = _photoTask.get();
                		sendDataToFile(Integer.toString(actualImages), "image-count.txt");
                		StringBuilder selected = new StringBuilder();

                		// for each selected chunk, store its name in a StringBuilder and save it to a file
                		for (Audio a : rightChunkList) {
                		    String filename = a.getName();
                		    selected.append(filename);
                		    selected.append(File.pathSeparator);
                        }
                		sendDataToFile(selected.toString(), "selected-chunks.txt");

                		setLoadingInactive();
                		changeScene(event, "/varpedia/PhotoPickerScreen.fxml");
                	} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
                });
                _photoTask.setOnCancelled(ev -> {
                    _photoTask = null;
                    setLoadingInactive();
                });
                _photoTask.setOnFailed(ev -> {
                    _alertHelper.showAlert(Alert.AlertType.ERROR, "Failed to download images.");
                    _photoTask = null;
                    setLoadingInactive();
                });
            	pool.submit(_photoTask);
            	setLoadingActive();
        	}
    	} else {
    		_photoTask.cancel(true);
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
            if (_photoTask != null && _photoTask.isRunning()) {
                _photoTask.cancel();
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
        Audio selectedChunk = leftChunkListView.getSelectionModel().getSelectedItem();
        // if something is selected in leftChunkList, shift it to rightChunkList
        if (selectedChunk != null) {
            rightChunkList.add(selectedChunk);
            leftChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressRemoveFromButton(ActionEvent event) {
        Audio selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
        // if something is selected in rightChunkList, shift it to leftChunkList
        if (selectedChunk != null) {
            leftChunkList.add(selectedChunk);
            rightChunkList.remove(selectedChunk);
        }
    }

    @FXML
    private void pressMoveUpButton(ActionEvent event) {
        Audio selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
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
        Audio selectedChunk = rightChunkListView.getSelectionModel().getSelectedItem();
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
     * This relies on serialized objects that were generated in the previous screen.
     */
    private void populateList() {
        Task<List<String>> task = new ListPopulateTask(new File("appfiles/audio"), ".wav");
        task.setOnSucceeded(event -> {
            try {
                List<String> chunks = task.get();
                if (chunks != null) {
                    // for each chunk, find its serialisation and add it to the list
                    for (String s : chunks) {
                        File chunkFile = new File("appfiles/audio/" + s + ".dat");

                        if (chunkFile.exists()) {
                            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chunkFile))) {
                                leftChunkList.add((Audio) ois.readObject());
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // if somehow a chunk hasn't been serialized, add it with its filename for display
                            leftChunkList.add(new Audio(s, s));		// this scenario should never happen!
                        }
                    }
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
        addToBtn.disableProperty().unbind();
        removeFromBtn.disableProperty().unbind();
        moveUpBtn.disableProperty().unbind();
        //moveDownBtn.disableProperty().unbind();

        selectBtn.setText("Stop");
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        backBtn.setDisable(true);
        loadingBar.setVisible(true);
        loadingLabel.setVisible(true);
    }

    /**
     * Helper method to enable most UI elements and hide loading indicators when a creation task ends.
     */
    private void setLoadingInactive() {
        // disable add to and remove from buttons until respective chunks are selected
        addToBtn.disableProperty().bind(leftChunkListView.getSelectionModel().selectedItemProperty().isNull());
        removeFromBtn.disableProperty().bind(rightChunkListView.getSelectionModel().selectedItemProperty().isNull());
        moveUpBtn.disableProperty().bind(Bindings.equal(0,rightChunkListView.getSelectionModel().selectedIndexProperty()));
        //moveDownBtn.disableProperty().bind(Bindings.equal(rightChunkList.size(),rightChunkListView.getSelectionModel().selectedIndexProperty()));

        selectBtn.setText("Select Photos");
        moveDownBtn.setDisable(false);
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
