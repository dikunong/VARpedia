package varpedia.controllers;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import varpedia.AlertHelper;
import varpedia.VARpediaApp;
import varpedia.tasks.WikitSearchTask;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Controller for the WikitSearchScreen, which handles the searching of Wikipedia for a given search term (via wikit).
 *
 * @author Di Kun Ong and Tudor Zagreanu
 */
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
    private AlertHelper _alertHelper = AlertHelper.getInstance();

    private Task<Boolean> _wikitTask;

    @FXML
    private void initialize() {
    	if (new File("appfiles/search-term.txt").exists()) {
    		searchTextField.setText(getDataFromFile("search-term.txt"));
    	}
    	
        setLoadingInactive();
    }

    @FXML
    private void pressSearchButton(ActionEvent event) {
        String searchTerm = searchTextField.getText();

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
                    _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "No valid Wikipedia articles found, or there might be a network issue.");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // handle wikit returning a disambiguation result, which doesn't output to stdout or stderr
        // and so can't be handled with regular logic
        _wikitTask.setOnFailed(event2 -> {
        	setLoadingInactive();
            _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Search timed out - search term may be too ambiguous or there might be a network issue.");
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
        searchBtn.disableProperty().unbind();
        searchBtn.setDisable(true);
        loadingLabel.setVisible(true);
        loadingWheel.setVisible(true);
    }

    /**
     * Helper method to enable most UI elements and hide loading indicators when a Wikit task ends.
     */
    private void setLoadingInactive() {
        // disable search button until a search term is typed
        searchBtn.disableProperty().bind(Bindings.isEmpty(searchTextField.textProperty()));
        loadingLabel.setVisible(false);
        loadingWheel.setVisible(false);
    }
}
