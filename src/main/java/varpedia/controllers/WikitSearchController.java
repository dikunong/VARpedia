package varpedia.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import varpedia.VARpediaApp;
import varpedia.tasks.WikitSearchTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class WikitSearchController extends Controller {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private ProgressIndicator loadingWheel;
    @FXML
    private Label loadingLabel;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();

    private Task<Boolean> _wikitTask;

    @FXML
    private void initialize() {
        setLoadingInactive();
    }

    @FXML
    private void pressSearchButton(ActionEvent event) {
        // check if there is text in the text field
        String searchTerm = searchTextField.getText();
        if (searchTerm.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in a valid search term.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            return;
        }

        // save search term into txt file for use later
        sendDataToFile(searchTerm, "search-term.txt");

        // perform Wikit search
        _wikitTask = new WikitSearchTask(searchTerm);

        _wikitTask.setOnSucceeded(event2 -> {
            try {
                boolean success = _wikitTask.get();
                if (success) {
                    // open TextEditorScreen
                    changeScene(event, "/varpedia/TextEditorScreen.fxml");
                } else {
                    setLoadingInactive();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No valid Wikipedia articles found.");
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    alert.showAndWait();
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        pool.submit(_wikitTask);

        // display loading indicator during search
        setLoadingActive();
    }

    @FXML
    private void pressCancelButton(ActionEvent event) {
        // if the WikitSearchTask exists and is running, cancel it
        if (_wikitTask != null && _wikitTask.isRunning()) {
            _wikitTask.cancel();
        }
        // open MainScreen
        changeScene(event, "/varpedia/MainScreen.fxml");
    }

    /**
     * Helper method to disable most UI elements and show loading indicators while a Wikit task is in progress.
     */
    private void setLoadingActive() {
        searchBtn.setDisable(true);
        loadingLabel.setVisible(true);
        loadingWheel.setVisible(true);
    }

    /**
     * Helper method to enable most UI elements and hide loading indicators when a Wikit task ends.
     */
    private void setLoadingInactive() {
        searchBtn.setDisable(false);
        loadingLabel.setVisible(false);
        loadingWheel.setVisible(false);
    }
}
