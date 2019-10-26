package varpedia.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import varpedia.helpers.AlertHelper;
import varpedia.models.Audio;
import varpedia.VARpediaApp;
import varpedia.tasks.FFMPEGAudioTask;
import varpedia.tasks.FFMPEGVideoTask;
import varpedia.tasks.PreviewAudioTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

/**
 * Controller for the PhotoAssembler, which handles selection of photos and background music, creation naming,
 * and assembly of the actual creation via FFMPEG.
 *
 * @author Di Kun Ong and Tudor Zagreanu
 */
public class PhotoPickerController extends Controller {

    @FXML
    private Button addToBtn;
    @FXML
    private Button removeFromBtn;
    @FXML
    private Button moveUpBtn;
    @FXML
    private Button moveDownBtn;
    @FXML
    private ChoiceBox<Audio> musicChoiceBox;
    @FXML
    private Label volLabel;
    @FXML
    private Slider volSlider;
    @FXML
    private TextField creationNameTextField;
    @FXML
    private Button createBtn;
    @FXML
    private Button previewBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button backBtn;
    @FXML
    private ProgressBar loadingBar;
    @FXML
    private Label loadingLabel;

    @FXML
    private ObservableList<Integer> leftPhotoList;
    @FXML
    private ListView<Integer> leftPhotoListView;
    @FXML
    private ObservableList<Integer> rightPhotoList;
    @FXML
    private ListView<Integer> rightPhotoListView;

    private Task<? extends Object> _createTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    
    private List<String> _chunks;

    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
    	setLoadingInactive();

    	// load in selected chunks from the ChunkAssembler
        _chunks = Arrays.asList(getDataFromFile("selected-chunks.txt").split(Pattern.quote(File.pathSeparator)));

        initializePhotos();

        // disable chunk lists if they are empty
        leftPhotoListView.disableProperty().bind(Bindings.size(leftPhotoList).isEqualTo(0));
        rightPhotoListView.disableProperty().bind(Bindings.size(rightPhotoList).isEqualTo(0));

    	// set up choicebox for background music
        List<Audio> musicList = new ArrayList<>();
        // retain the none option in memory for comparison for binding
        Audio noneAudio = new Audio(null, "None");
    	musicList.add(noneAudio);
    	musicList.add(new Audio("/varpedia/music/chinese.mp3", "Mandolin Chinese"));
    	musicList.add(new Audio("/varpedia/music/perspective.mp3", "Another Perspective"));
    	musicList.add(new Audio("/varpedia/music/sirius.mp3", "Sirius Crystal"));
    	musicChoiceBox.getItems().addAll(musicList);
        musicChoiceBox.getSelectionModel().selectFirst();
        
        // add the volume slider and set the default to 50% (the original value)
        volSlider.setValue(50);
    	
        volSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        	volLabel.setText(Math.round(newValue.doubleValue()) + "%");
        });

        // disable background volume slider if no background music is selected
        volSlider.disableProperty().bind(musicChoiceBox.valueProperty().isEqualTo(noneAudio));
        
        // disable create button if there is no creation name
        createBtn.disableProperty().bind(Bindings.isEmpty(creationNameTextField.textProperty()));
    }
    
    @FXML
    private void pressCreateBtn(ActionEvent event) {
    	if (_createTask == null) {
        	String name = creationNameTextField.getText();
        	String bgmusic = musicChoiceBox.getSelectionModel().getSelectedItem().getName();
		
	    	if (name == null || name.isEmpty()) {
	    	    // nothing happens if the text box is empty and the user presses Enter
                return;
            } else if (!name.matches("[-_. A-Za-z0-9]+")) {
	            _alertHelper.showAlert(Alert.AlertType.ERROR, "Invalid creation name", "Please enter a valid creation name (only letters, numbers, spaces, -, _).");
	        } else {
	        	// check if creation already exists and offer option to overwrite
	            // this must go here in order to allow application flow to continue if user chooses to overwrite
			    if (checkDuplicate(name)) {
			        _alertHelper.showAlert(Alert.AlertType.WARNING, "Confirm overwrite",
                            "Creation already exists. Overwrite?",
                            ButtonType.YES, ButtonType.CANCEL);
	                if (_alertHelper.getResult() == ButtonType.CANCEL) {
	                    return;
	                }
	            }
			    
			    // confirm if there should be no images (it's not an error, but could be confusing if it just made it)
			    if (rightPhotoList.isEmpty()) {
			    	_alertHelper.showAlert(Alert.AlertType.WARNING, "Confirm no images",
                            "Creation will have no images. Continue?",
                            ButtonType.YES, ButtonType.CANCEL);
	                if (_alertHelper.getResult() == ButtonType.CANCEL) {
	                    return;
	                }
			    }

                // assemble audio + video using ffmpeg
                _createTask = new FFMPEGAudioTask(_chunks, bgmusic, volSlider.getValue() / 100);
                _createTask.setOnSucceeded(ev2 -> {
                    _createTask = new FFMPEGVideoTask(name, rightPhotoList);
                    _createTask.setOnSucceeded(ev3 -> {
                        _createTask = null;
                        _alertHelper.showAlert(Alert.AlertType.INFORMATION, "Success", "Created creation.");
                        setLoadingInactive();
                        changeScene(event, "/varpedia/fxml/MainScreen.fxml"); //TODO: Maybe go straight to player
                    });
                    _createTask.setOnCancelled(ev3 -> {
                        _createTask = null;
                        setLoadingInactive();
                    });
                    _createTask.setOnFailed(ev3 -> {
                        _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to create creation.");
                        _createTask = null;
                        setLoadingInactive();
                    });
                    pool.submit(_createTask);
                });
                _createTask.setOnCancelled(ev2 -> {
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnFailed(ev2 -> {
                    _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to create creation.");
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
    private void pressPreviewBtn(ActionEvent event) {
    	if (_createTask == null) {
        	String bgmusic = musicChoiceBox.getSelectionModel().getSelectedItem().getName();
	        // assemble audio using ffmpeg
            _createTask = new FFMPEGAudioTask(_chunks, bgmusic, volSlider.getValue() / 100);
            _createTask.setOnSucceeded(ev2 -> {
                // play the assembled audio
            	_createTask = new PreviewAudioTask(new File("appfiles/audio.wav"));
                _createTask.setOnSucceeded(ev3 -> {
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnCancelled(ev3 -> {
                    _createTask = null;
                    setLoadingInactive();
                });
                _createTask.setOnFailed(ev3 -> {
                    _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to preview creation.");
                    _createTask = null;
                    setLoadingInactive();
                });
                pool.submit(_createTask);
            });
            _createTask.setOnCancelled(ev2 -> {
                _createTask = null;
                setLoadingInactive();
            });
            _createTask.setOnFailed(ev2 -> {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to preview creation.");
                _createTask = null;
                setLoadingInactive();
            });
            pool.submit(_createTask);
            setLoadingActivePreview();
    	} else {
    		_createTask.cancel(true);
    		setLoadingInactive();
    	}
    }
    
    @FXML
    private void pressCancelBtn(ActionEvent event) {
        // ask for confirmation first!
        _alertHelper.showAlert(Alert.AlertType.CONFIRMATION, "Confirm cancel",
                "Are you sure you want to cancel making the current creation?",
                ButtonType.YES, ButtonType.CANCEL);
        if (_alertHelper.getResult() == ButtonType.YES) {
            // if a creation is currently in progress, cancel it
            if (_createTask != null && _createTask.isRunning()) {
                _createTask.cancel();
            }
            // open MainScreen
            changeScene(event, "/varpedia/fxml/MainScreen.fxml");
        }
    }

    @FXML
    private void pressBackBtn(ActionEvent event) {
        // open TextEditorScreen - do not lose any progress
        changeScene(event, "/varpedia/fxml/ChunkAssemblerScreen.fxml");
    }

    @FXML
    private void pressAddToButton(ActionEvent event) {
        Integer selectedPhoto = leftPhotoListView.getSelectionModel().getSelectedItem();
        // if something is selected in leftPhotoList, shift it to rightPhotoList
        if (selectedPhoto != null) {
            rightPhotoList.add(selectedPhoto);
            leftPhotoList.remove(selectedPhoto);
        }
    }

    @FXML
    private void pressRemoveFromButton(ActionEvent event) {
    	Integer selectedPhoto = rightPhotoListView.getSelectionModel().getSelectedItem();
        // if something is selected in righttPhotoList, shift it to leftPhotoList
        if (selectedPhoto != null) {
            leftPhotoList.add(selectedPhoto);
            rightPhotoList.remove(selectedPhoto);
        }
    }

    @FXML
    private void pressMoveUpButton(ActionEvent event) {
    	Integer selectedPhoto = rightPhotoListView.getSelectionModel().getSelectedItem();
        int selectedIndex = rightPhotoListView.getSelectionModel().getSelectedIndex();
        // if something is selected in rightPhotoList and it's not already first, shift its index by -1
        if (selectedPhoto != null && selectedIndex > 0) {
            rightPhotoList.remove(selectedIndex);
            rightPhotoList.add(selectedIndex - 1, selectedPhoto);
            rightPhotoListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    private void pressMoveDownButton(ActionEvent event) {
    	Integer selectedPhoto = rightPhotoListView.getSelectionModel().getSelectedItem();
        int selectedIndex = rightPhotoListView.getSelectionModel().getSelectedIndex();
        int maxIndex = rightPhotoListView.getItems().size() - 1;
        // if something is selected in rightPhotoList and it's not already last, shift its index by +1
        if (selectedPhoto != null && selectedIndex < maxIndex) {
            rightPhotoList.remove(selectedIndex);
            rightPhotoList.add(selectedIndex + 1, selectedPhoto);
            rightPhotoListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    /**
     * Helper method that initializes the display of photos in the ListViews by defining the display
     * of images, before actually loading them in.
     */
    private void initializePhotos() {
        int images = Integer.parseInt(getDataFromFile("image-count.txt"));

        // set up the left and right list views to hold ImageViews

        leftPhotoListView.setCellFactory(param -> new ListCell<Integer>() {
            private ImageView imageView = new ImageView();

            // removes horizontal scrollbar from listview
            {
                setPrefWidth(0);
            }

            @Override
            public void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // set the image to be no taller than 150 pixels but as wide
                    // as possible while preserving aspect ratio
                    imageView.setImage(new Image(new File("appfiles/image" + id + ".jpg").toURI().toString()));
                    imageView.setPreserveRatio(true);
                    imageView.fitWidthProperty().bind(leftPhotoListView.widthProperty().subtract(30));
                    imageView.setFitHeight(150);
                    setText(null);
                    setGraphic(imageView);
                }
            }
        });

        rightPhotoListView.setCellFactory(param -> new ListCell<Integer>() {
            private ImageView imageView = new ImageView();

            // removes horizontal scrollbar from listview
            {
                setPrefWidth(0);
            }

            @Override
            public void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // set the image to be no taller than 150 pixels but as wide
                    // as possible while preserving aspect ratio
                    imageView.setImage(new Image(new File("appfiles/image" + id + ".jpg").toURI().toString()));
                    imageView.setPreserveRatio(true);
                    imageView.fitWidthProperty().bind(rightPhotoListView.widthProperty().subtract(30));
                    imageView.setFitHeight(150);
                    setText(null);
                    setGraphic(imageView);
                }
            }
        });

        for (int i = 0; i < images; i++) {
            leftPhotoList.add(i);
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
     * Helper method to disable most UI elements and show loading indicators while a creation task is in progress.
     */
    private void setLoadingActive() {
        addToBtn.disableProperty().unbind();
        removeFromBtn.disableProperty().unbind();
        moveUpBtn.disableProperty().unbind();

        createBtn.setText("Stop");
        previewBtn.setDisable(true);
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        creationNameTextField.setDisable(true);
        backBtn.setDisable(true);
        loadingBar.setVisible(true);
        loadingLabel.setVisible(true);
    }

    /**
     * Helper method to disable most UI elements and show loading indicators while a preview task is in progress.
     */
    private void setLoadingActivePreview() {
        addToBtn.disableProperty().unbind();
        removeFromBtn.disableProperty().unbind();
        moveUpBtn.disableProperty().unbind();
        createBtn.disableProperty().unbind();
        
        previewBtn.setText("Stop");
        createBtn.setDisable(true);
        addToBtn.setDisable(true);
        removeFromBtn.setDisable(true);
        moveUpBtn.setDisable(true);
        moveDownBtn.setDisable(true);
        creationNameTextField.setDisable(true);
        backBtn.setDisable(true);
        loadingBar.setVisible(true);
        loadingLabel.setVisible(true);
    }
    
    /**
     * Helper method to enable most UI elements and hide loading indicators when a creation/creation task ends.
     */
    private void setLoadingInactive() {
        // disable add to and remove from buttons until respective chunks are selected
    	addToBtn.disableProperty().bind(leftPhotoListView.getSelectionModel().selectedItemProperty().isNull());
        removeFromBtn.disableProperty().bind(rightPhotoListView.getSelectionModel().selectedItemProperty().isNull());
        moveUpBtn.disableProperty().bind(Bindings.equal(0,rightPhotoListView.getSelectionModel().selectedIndexProperty()));
        
        // disable create button until there is a name
        createBtn.disableProperty().bind(Bindings.isEmpty(creationNameTextField.textProperty()));
        
        createBtn.setText("Create!");
        previewBtn.setText("Preview");
        previewBtn.setDisable(false);
        moveDownBtn.setDisable(false);
        creationNameTextField.setDisable(false);
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
