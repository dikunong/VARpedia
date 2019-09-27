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
            changeScene(event, "../PlaybackScreen.fxml");
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
                //setSelectedCreation();
                // delete creation file
            }
        }
    }

    @FXML
    private void pressCreateButton(ActionEvent event) {
        // open WikitSearchScreen
        changeScene(event, "../WikitSearchScreen.fxml");
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

    /*public void setSelectedCreation() {
        _selectedFile = "creations/" + creationListView.getSelectionModel().getSelectedItem();
    }*/

    private void populateList() {
        File creationsDir = new File("creations");

        // ensure the creations directory is actually a directory AND exists
        if (creationsDir.isFile()) {
            creationsDir.delete();
        }
        if (!creationsDir.exists()) {
            creationsDir.mkdir();
        }

        Task<List<String>> task = new ListPopulateTask(creationsDir);
        // VARpediaApp.submit(task);
        // ^ that doesn't work at the moment, but hopefully in the next few hours it will
        Thread thread = new Thread(task);
        thread.start();

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


    }

}
