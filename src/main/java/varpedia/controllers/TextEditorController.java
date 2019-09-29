package varpedia.controllers;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.VARpediaApp;
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
    private ProgressIndicator loadingWheel;
    @FXML
    private Label loadingLabel;

    private Task<Void> _playTask;
    private Task<Void> _saveTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    @FXML
    private void initialize() {
        setLoadingInactive();

    	//TODO: Do this properly
    	Task<String[]> dat = new VoiceListTask();
    	dat.run();
    	
    	try {
			voiceChoiceBox.getItems().addAll(dat.get());
			voiceChoiceBox.getSelectionModel().selectFirst();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

    	// get search-output.txt generated by WikitSearchTask
        // note to self - exceptions should probably be caught here so error messages
        // can be given to the user rather than just printing stack traces
        String searchOutput = getDataFromFile("search-output.txt");
        wikiTextArea.setText(searchOutput);
    }

    //TODO: Repetition between this and save
    @FXML
    private void pressPreviewButton(ActionEvent event) {
        if (_playTask == null) {
    		// get selected text from wikiTextArea
            String text = wikiTextArea.getSelectedText();

    		if (text == null || text.isEmpty()) {
    			Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text first.");
                alert.showAndWait();
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				Alert alert = new Alert(Alert.AlertType.WARNING, "Selected text is very long (>30 words). Do you wish to continue anyway?", ButtonType.YES, ButtonType.CANCEL);
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
		            	setLoadingInactive();
		            });
		            _playTask.setOnCancelled(ev -> {
		                _playTask = null;
		            	previewBtn.setText("Preview");
		            	setLoadingInactive();
			        });
		            _playTask.setOnFailed(ev -> {
		            	Alert alert = new Alert(Alert.AlertType.ERROR, "Error playing audio chunk. Try selecting other text or using a different voice.");
		                alert.showAndWait();
		                _playTask = null;
		            	previewBtn.setText("Preview");
		            	setLoadingInactive();
			        });
		            pool.submit(_playTask);
		            setLoadingActive();
		            previewBtn.setText("Stop Preview");
    			}
    		}
    	} else {
    		_playTask.cancel(true);
    		setLoadingInactive();
    	}
    }

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
    			Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text first.");
                alert.showAndWait();
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				Alert alert = new Alert(Alert.AlertType.WARNING, "Selected text is very long (>30 words). Do you wish to continue anyway?", ButtonType.YES, ButtonType.CANCEL);
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
		                setLoadingInactive();
				    });
		    		_saveTask.setOnCancelled(ev -> {
		                _saveTask = null;
		                saveBtn.setText("Save Chunk");
		                setLoadingInactive();
					});
		            _saveTask.setOnFailed(ev -> {
		    			Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving audio chunk. Try selecting other text or using a different voice.");
		                alert.showAndWait();
		                _saveTask = null;
		                saveBtn.setText("Save Chunk");
		                setLoadingInactive();
					});
		            pool.submit(_saveTask);
		            saveBtn.setText("Cancel Saving");
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
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            changeScene(event, "/varpedia/MainScreen.fxml");
        }
    }

    private void setLoadingActive() {
        assembleBtn.setDisable(true);
        previewBtn.setDisable(true);
        voiceChoiceBox.setDisable(true);
        loadingLabel.setVisible(true);
        loadingWheel.setVisible(true);
    }

    private void setLoadingInactive() {
        assembleBtn.setDisable(false);
        previewBtn.setDisable(false);
        voiceChoiceBox.setDisable(false);
        loadingLabel.setVisible(false);
        loadingWheel.setVisible(false);
    }

}
