package varpedia.controllers;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import varpedia.VARpediaApp;
import varpedia.tasks.PlayChunkTask;
import varpedia.tasks.VoiceListTask;

/**
 * Controller for the TextEditorScreen, which handles the selection and creation of audio "chunks" from the Wikipedia
 * search results, including selecting any available festival voice synthesizer.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
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
    private ProgressIndicator loadingWheel;
    @FXML
    private Label loadingLabel;

    private Task<Void> _playTask;
    private Task<Void> _saveTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    @FXML
    private void initialize() {
        setLoadingInactive();

    	Task<String[]> dat = new VoiceListTask();
    	dat.run();
    	
    	try {
			voiceChoiceBox.getItems().addAll(dat.get());
			voiceChoiceBox.getSelectionModel().selectFirst();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

    	// get search-output.txt generated by WikitSearchTask
        String searchOutput = getDataFromFile("search-output.txt");
        wikiTextArea.setText(searchOutput);
    }

    @FXML
    private void pressPreviewButton(ActionEvent event) {
        if (_playTask == null) {
    		// get selected text from wikiTextArea
            String text = wikiTextArea.getSelectedText();

    		if (text == null || text.isEmpty()) {
    			showNotifyingAlert(Alert.AlertType.ERROR, "Please select some text first.");
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				Alert alert = new Alert(Alert.AlertType.WARNING, "Selected text is very long (>30 words). Do you wish to continue anyway?", ButtonType.YES, ButtonType.CANCEL);
					alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    				alert.showAndWait();
    	            playText = alert.getResult() == ButtonType.YES;
    			} else {
    				playText = true;
    			}

    			if (playText) {
    				// play back selected text in Festival using selected Voice
    		    	_playTask = new PlayChunkTask(text, null, voiceChoiceBox.getSelectionModel().getSelectedItem());
		            _playTask.setOnSucceeded(ev -> {
		            	_playTask = null;
		            	previewBtn.setText("Preview");
		            	saveBtn.setDisable(false);
		            	setLoadingInactive();
		            });
		            _playTask.setOnCancelled(ev -> {
		                _playTask = null;
		            	previewBtn.setText("Preview");
						saveBtn.setDisable(false);
		            	setLoadingInactive();
			        });
		            _playTask.setOnFailed(ev -> {
		            	showNotifyingAlert(Alert.AlertType.ERROR, "Error playing audio chunk. Try selecting other text or using a different voice.");
		                _playTask = null;
		            	previewBtn.setText("Preview");
						saveBtn.setDisable(false);
		            	setLoadingInactive();
			        });
		            pool.submit(_playTask);
		            setLoadingActive();
		            previewBtn.setText("Stop");
		            saveBtn.setDisable(true);
    			}
    		}
    	} else {
    		_playTask.cancel(true);
    		setLoadingInactive();
    	}
    }

	/**
	 * Helper method that converts a segment of the chunk text into a suitable filename, by stripping invalid
	 * characters and replacing spaces with underscores.
	 * @param text Text to be converted
	 * @return Filename-safe text
	 */
	private String getFileName(String text) {
    	String clean = text.replaceAll("[^A-Za-z0-9\\-_ ]", "").replace(' ', '_');
    	String name = "appfiles/audio/" + clean.substring(0, Math.min(clean.length(), 32));
    	String str = name + ".wav";

    	if (new File(str).exists()) {
    		int id = 2;

    		do {
    			str = name + "_" + id + ".wav";
    			id++;
    	    } while (new File(str).exists());
    	}

    	return str;
    }

    @FXML
    private void pressSaveButton(ActionEvent event) {
        if (_saveTask == null) {
        	// get selected text from wikiTextArea
            String text = wikiTextArea.getSelectedText();

    		if (text == null || text.isEmpty()) {
				showNotifyingAlert(Alert.AlertType.ERROR, "Please select some text first.");
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				Alert alert = new Alert(Alert.AlertType.WARNING, "Selected text is very long (>30 words). Do you wish to continue anyway?", ButtonType.YES, ButtonType.CANCEL);
					alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    				alert.showAndWait();
    	            playText = alert.getResult() == ButtonType.YES;
    			} else {
    				playText = true;
    			}

    			if (playText) {
    				String filename = getFileName(text);

    				// save selected text audio into .wav "chunk"
    		        _saveTask = new PlayChunkTask(text, filename, voiceChoiceBox.getSelectionModel().getSelectedItem());
		    		_saveTask.setOnSucceeded(ev -> {
		                _saveTask = null;
		                saveBtn.setText("Save Chunk");
		                previewBtn.setDisable(false);
		                setLoadingInactive();
				    });
		    		_saveTask.setOnCancelled(ev -> {
		                _saveTask = null;
		                saveBtn.setText("Save Chunk");
						previewBtn.setDisable(false);
		                setLoadingInactive();
					});
		            _saveTask.setOnFailed(ev -> {
						showNotifyingAlert(Alert.AlertType.ERROR, "Error saving audio chunk. Try selecting other text or using a different voice.");
		                _saveTask = null;
		                saveBtn.setText("Save Chunk");
						previewBtn.setDisable(false);
		                setLoadingInactive();
					});
		            pool.submit(_saveTask);
		            saveBtn.setText("Stop");
					previewBtn.setDisable(true);
		            setLoadingActive();
		        }
    		}
    	} else {
    		_saveTask.cancel(true);
    		setLoadingInactive();
    	}
    }

    @FXML
    private void pressAssembleButton(ActionEvent event) {
        // open ChunkAssemblerScreen
        changeScene(event, "/varpedia/ChunkAssemblerScreen.fxml");
    }

    @FXML
    private void pressCancelButton(ActionEvent event) {
        // ask for confirmation first!
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel making " +
                "the current creation?", ButtonType.YES, ButtonType.CANCEL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
			// if a chunk is currently being played, cancel it
			if (_playTask != null && _playTask.isRunning()) {
				_playTask.cancel();
			}
			// if a chunk is currently being saved, cancel it
			if (_saveTask != null && _saveTask.isRunning()) {
				_saveTask.cancel();
			}
            changeScene(event, "/varpedia/MainScreen.fxml");
        }
    }

	/**
	 * Helper method to disable most UI elements and show loading indicators while an audio chunk task is in progress.
	 */
    private void setLoadingActive() {
        assembleBtn.setDisable(true);
        voiceChoiceBox.setDisable(true);
        loadingLabel.setVisible(true);
        loadingWheel.setVisible(true);
    }

	/**
	 * Helper method to enable most UI elements and hide loading indicators when an audio chunk task ends.
	 */
    private void setLoadingInactive() {
        assembleBtn.setDisable(false);
        voiceChoiceBox.setDisable(false);
        loadingLabel.setVisible(false);
        loadingWheel.setVisible(false);
    }

}
