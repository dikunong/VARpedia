package varpedia.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import varpedia.AlertHelper;
import varpedia.Creation;
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
    private final ObservableList<Creation> creationList = FXCollections.observableArrayList();
    @FXML
    private TableView<Creation> creationTableView;
    @FXML
    private TableColumn<Creation, String> creationNameCol;
    @FXML
    private TableColumn<Creation, String> creationConfCol;
    @FXML
    private TableColumn<Creation, String> creationViewCol;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
        // populate table view with saved creations
        creationNameCol.setCellValueFactory(new PropertyValueFactory<Creation, String>("creationName"));
        creationConfCol.setCellValueFactory(new PropertyValueFactory<Creation, String>("confidence"));
        creationViewCol.setCellValueFactory(new PropertyValueFactory<Creation, String>("lastViewed"));
        populateTable();
        deleteAppfiles();

        // disable play and delete buttons until a creation is selected
        playBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
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
            // delete creation file
            File file = new File("creations/" + getSelectedFilename());
            if (file.delete()) {
                // update table view
                creationList.remove(creationTableView.getSelectionModel().getSelectedItem());
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
        return creationTableView.getSelectionModel().getSelectedItem().getCreationName() + ".mp4";
    }

    /**
     * Helper method that retrives the observable list for the FXML component.
     * @return observable list of Creations
     */
    public ObservableList<Creation> getCreationList() {
        return creationList;
    }

    /**
     * Helper method that runs a task to populate the creationList with chunks in the creations directory.
     */
    private void populateTable() {
        // This method re-populates the List everytime and cannot obtain confidence and lastViewed data
        // need a new method that can retain this data even between restarts of the app
        // TODO: redo completely to use serialization of creation objects

        Task<List<String>> task = new ListPopulateTask(new File("creations"), ".mp4");
        task.setOnSucceeded(event -> {
            try {
                List<String> newCreations = task.get();
                if (newCreations != null) {
                    for (String s : newCreations) {
                        creationList.add(new Creation(s, null, null));
                    }
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
