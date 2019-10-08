package varpedia.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import varpedia.AlertHelper;
import varpedia.VARpediaApp;
import varpedia.tasks.ClearTask;
import varpedia.tasks.ListPopulateTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Controller for the MainScreen, which displays current creations that can be played and deleted, as well as acting as
 * the gateway to making new creations.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
public class MainController extends Controller {

    @FXML
    private Button playBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button createBtn;

    @FXML
    private ObservableList<String> creationList;
    @FXML
    private ListView<String> creationListView;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
        // populate list view with saved creations
        populateList();
        deleteAppfiles();

        // disable play and delete buttons until a creation is selected
        playBtn.disableProperty().bind(creationListView.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(creationListView.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // store a creation's filename and open PlaybackScreen
        sendDataToFile("creations/" + getSelectedFilename(), "playback-name.txt");
        changeScene(event, "/varpedia/PlaybackScreen.fxml");
}

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // ask for confirmation
        _alertHelper.showAlert(Alert.AlertType.WARNING,
                "Are you sure you want to delete the selected creation?", // add selected creation name here later
                ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
            String filename = getSelectedFilename();
            // delete creation file
            File file = new File("creations/" + filename);
            if (file.delete()) {
                // update list view
                creationList.remove(filename.substring(0, filename.lastIndexOf('.')));
            } else {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Could not delete file.");
            }
        }
    }

    @FXML
    private void pressCreateButton(ActionEvent event) {
        // open WikitSearchScreen
        changeScene(event, "/varpedia/WikitSearchScreen.fxml");
    }

    /**
     * Helper method that retrieves a filename of a selected creation.
     * @return Creation filename
     */
    private String getSelectedFilename() {
        return creationListView.getSelectionModel().getSelectedItem() + ".mp4";
    }

    /**
     * Helper method that runs a task to populate the creationList with chunks in the creations directory.
     */
    private void populateList() {
        Task<List<String>> task = new ListPopulateTask(new File("creations"));
        task.setOnSucceeded(event -> {
            try {
                List<String> newCreations = task.get();
                if (newCreations != null) {
                    creationList.addAll(newCreations);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        pool.submit(task);
    }

    //TODO: This is running in the GUI thread to avoid race conditions. Could move it out.
    private void deleteAppfiles() {
    	Task<Void> task = new ClearTask(new File("appfiles"));
    	task.run();
    }
}
