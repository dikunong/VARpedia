package varpedia.controllers;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.AlertHelper;
import varpedia.Audio;
import varpedia.VARpediaApp;
import varpedia.VoiceList;
import varpedia.tasks.ListPopulateTask;
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
    private ChoiceBox<Audio> voiceChoiceBox;
    @FXML
    private ProgressIndicator loadingWheel;
    @FXML
    private Label loadingLabel;
    @FXML
	private ObservableList<Audio> chunkList;
    @FXML
	private ListView<Audio> chunkListView;

    private Task<Void> _playTask;
    private Task<Void> _saveTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
        setLoadingInactive();
        populateList();

    	Task<VoiceList> dat = new VoiceListTask();
    	dat.run();
    	
    	try {
    		VoiceList list = dat.get();
			voiceChoiceBox.getItems().addAll(list._voices);
			
			if (list._defaultVoice == -1) {
				voiceChoiceBox.getSelectionModel().selectFirst();
			} else {
				voiceChoiceBox.getSelectionModel().select(list._defaultVoice);
			}
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
    			_alertHelper.showAlert(Alert.AlertType.ERROR, "Please select some text first.");
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				_alertHelper.showAlert(Alert.AlertType.WARNING,
							"Selected text is very long (>30 words). Do you wish to continue anyway?",
							ButtonType.YES, ButtonType.CANCEL);

    	            playText = _alertHelper.getResult() == ButtonType.YES;
    			} else {
    				playText = true;
    			}

    			if (playText) {
    				// play back selected text in Festival using selected Voice
    		    	_playTask = new PlayChunkTask(text, null, voiceChoiceBox.getSelectionModel().getSelectedItem().getName());
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
		            	_alertHelper.showAlert(Alert.AlertType.ERROR, "Error playing audio chunk. Try selecting other text or using a different voice.");
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

    @FXML
    private void pressSaveButton(ActionEvent event) {
        if (_saveTask == null) {
        	// get selected text from wikiTextArea
            String text = wikiTextArea.getSelectedText();

    		if (text == null || text.isEmpty()) {
				_alertHelper.showAlert(Alert.AlertType.ERROR, "Please select some text first.");
    		} else {
    			boolean playText;

    			if (text.split(" ").length > 30) {
    				_alertHelper.showAlert(Alert.AlertType.WARNING,
							"Selected text is very long (>30 words). Do you wish to continue anyway?",
							ButtonType.YES, ButtonType.CANCEL);

    	            playText = _alertHelper.getResult() == ButtonType.YES;
    			} else {
    				playText = true;
    			}

    			if (playText) {
    				String filename = getFilename(text, true);

    				// save selected text audio into .wav "chunk"
    		        _saveTask = new PlayChunkTask(text, filename, voiceChoiceBox.getSelectionModel().getSelectedItem().getName());
		    		_saveTask.setOnSucceeded(ev -> {
		                _saveTask = null;

						// create the user-friendly chunk name - trim if selected text is too long
						String chunkName = getFilename(text, false);
						String displayText;
						if (text.length() > 32) {
							displayText = text.substring(0, 32) + "...";
						} else {
							displayText = text;
						}

						// create and serialize new Audio object representing the chunk
						Audio newChunk = new Audio(chunkName, displayText);
						chunkList.add(newChunk);

						try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(chunkName + ".dat")))) {
							oos.writeObject(newChunk);
						} catch (IOException e) {
							e.printStackTrace();
						}

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
						_alertHelper.showAlert(Alert.AlertType.ERROR, "Error saving audio chunk. Try selecting other text or using a different voice.");
						_saveTask.getException().printStackTrace();
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
		_alertHelper.showAlert(Alert.AlertType.CONFIRMATION,
				"Are you sure you want to cancel making the current creation?",
				ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
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
	 * Helper method that converts a segment of the chunk text into a suitable filename, by stripping invalid
	 * characters and replacing spaces with underscores.
	 * @param text Text to be converted
	 * @param getDir if true, returns string with .dir extension (false is for serialization)
	 * @return Filename-safe text
	 */
	private String getFilename(String text, boolean getDir) {
		String clean = text.replaceAll("[^A-Za-z0-9\\-_ ]", "").replace(' ', '_');
		String name = "appfiles/audio/" + clean.substring(0, Math.min(clean.length(), 32));

		if (getDir) {
			String str = name + ".dir";

			if (new File(str).exists()) {
				int id = 2;

				do {
					str = name + "_" + id + ".dir";
					id++;
				} while (new File(str).exists());
			}

			return str;
		} else {
			return name;
		}
	}

	/**
	 * Helper method that runs a task to populate the chunkList with chunks in the appfiles/audio directory.
	 * This is only useful if the user decides to come back to this screen from the ChunkAssembler.
	 */
	private void populateList() {
		Task<List<String>> task = new ListPopulateTask(new File("appfiles/audio"), ".wav");
		task.setOnSucceeded(event -> {
			try {
				List<String> newCreations = task.get();
				if (newCreations != null) {
					for (String s : newCreations) {
						File creationFile = new File("appfiles/audio/" + s + ".dat");

						if (creationFile.exists()) {
							try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(creationFile))) {
								chunkList.add((Audio) ois.readObject());
							} catch (IOException | ClassNotFoundException e) {
								e.printStackTrace();
							}
						} else {
							chunkList.add(new Audio(s, s));		// this scenario should never happen!
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
