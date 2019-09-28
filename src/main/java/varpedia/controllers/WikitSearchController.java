package varpedia.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.tasks.WikitSearchTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private ExecutorService pool = Executors.newCachedThreadPool();

    private Task<Boolean> _wikitTask;

    @FXML
    private void initialize() {
        loadingWheel.setVisible(false);
        loadingLabel.setVisible(false);
    }

    @FXML
    private void pressSearchButton(ActionEvent event) {
        // check if there is text in the text field
        // in the future, could apply regex to prevent searching of characters Wikipedia has blocked from
        // being in titles, which are: # < > [ ] | { }
        // but that's a nice-to-have as the search will still fail gracefully without it
        String searchTerm = searchTextField.getText();
        if (searchTerm.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in a valid search term.");
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
                    searchBtn.setDisable(false);
                    loadingLabel.setVisible(false);
                    loadingWheel.setVisible(false);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No valid Wikipedia articles found.");
                    alert.showAndWait();
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        pool.submit(_wikitTask);
        searchBtn.setDisable(true);
        // display loading indicator during search
        loadingLabel.setVisible(true);
        loadingWheel.setVisible(true);
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
}
