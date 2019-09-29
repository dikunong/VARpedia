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
import varpedia.VARpediaApp;
import varpedia.tasks.ListPopulateTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

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

    @FXML
    private void initialize() {
        // populate list view with saved creations
        populateList();
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // if a creation is actually selected, store its filename and open PlaybackScreen
        if (checkCreationSelected()) {
            sendDataToFile("creations/" + getSelectedFilename(), "playback-name.txt");
            changeScene(event, "/varpedia/PlaybackScreen.fxml");
        }
}

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // check if an item is actually selected first
        if (checkCreationSelected()) {
            // ask for confirmation
            Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete the " +
                    "selected creation?", ButtonType.YES, ButtonType.CANCEL); // add selected creation name here later
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                String filename = getSelectedFilename();
                // delete creation file
                File file = new File("creations/" + filename);
                if (file.delete()) {
                    // update list view
                    creationList.remove(filename.substring(0, filename.lastIndexOf('.')));
                } else {
                    alert = new Alert(Alert.AlertType.ERROR, "Could not delete file.");
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    alert.showAndWait();
                }
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
     * Helper method that checks if a creation is currently selected in the ListView
     * @return true if creation is selected
     */
    private boolean checkCreationSelected() {
        // check if an item is actually selected first
        if (creationListView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a creation first.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            return false;
        } else {
            return true;
        }
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

}
