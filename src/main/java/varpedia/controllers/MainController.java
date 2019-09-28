package varpedia.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import varpedia.tasks.ListPopulateTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private ExecutorService pool = Executors.newCachedThreadPool();

    @FXML
    private void initialize() {
        populateList();
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // check if an item is actually selected first
        if (checkCreationSelected()) {
            sendDataToFile("creations/" + creationListView.getSelectionModel().getSelectedItem());
            // open PlaybackScreen
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
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                String filename = creationListView.getSelectionModel().getSelectedItem();
                // delete creation file
                File file = new File("creations/" + filename);
                if (file.delete()) {
                    // update listview
                    creationList.remove(filename);
                    return;
                } else {
                    alert = new Alert(Alert.AlertType.ERROR, "Could not delete file.");
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

    private boolean checkCreationSelected() {
        // check if an item is actually selected first
        if (creationListView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a creation first.");
            alert.showAndWait();
            return false;
        } else {
            return true;
        }
    }

    private void populateList() {
        Task<List<String>> task = new ListPopulateTask(new File("creations"));
        task.setOnSucceeded(event -> {
            try {
                List<String> newCreations = task.get();
                if (newCreations != null) {
                    //creationList.clear(); - use this later if not adding/removing creations individually
                    creationList.addAll(newCreations);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        pool.submit(task);
    }

}
