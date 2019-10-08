package varpedia.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.VARpediaApp;

import java.util.concurrent.ExecutorService;

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
    private TextField creationNameTextField;
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
    private ObservableList<String> leftPhotoList;
    @FXML
    private ListView<String> leftPhotoListView;
    @FXML
    private ObservableList<String> rightPhotoList;
    @FXML
    private ListView<String> rightPhotoListView;

    private Task<? extends Object> _createTask;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    @FXML
    private void initialize() {
        // load images into list view
    }

    @FXML
    private void pressCreateBtn(ActionEvent event) {

    }

    @FXML
    private void pressCancelBtn(ActionEvent event) {
        // ask for confirmation first
        // if a creation is currently in progress, cancel it
        // open MainScreen
    }

    @FXML
    private void pressBackBtn(ActionEvent event) {
        // open TextEditorScreen - do not lose any progress
        changeScene(event, "/varpedia/ChunkAssemblerScreen.fxml");
    }

    @FXML
    private void pressAddToButton(ActionEvent event) {
        // if something is selected in leftChunkList, shift it to rightChunkList
    }

    @FXML
    private void pressRemoveFromButton(ActionEvent event) {
        // if something is selected in rightChunkList, shift it to leftChunkList
    }

    @FXML
    private void pressMoveUpButton(ActionEvent event) {
        // if something is selected in rightChunkList and it's not already first, shift its index by -1
    }

    @FXML
    private void pressMoveDownButton(ActionEvent event) {
        // if something is selected in rightChunkList and it's not already last, shift its index by +1
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
        backBtn.setDisable(false);
        loadingBar.setVisible(false);
        loadingLabel.setVisible(false);
    }
}
