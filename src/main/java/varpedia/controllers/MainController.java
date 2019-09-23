package varpedia.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

public class MainController extends Controller {

    @FXML
    private Button playBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button createBtn;
    @FXML
    private ListView<String> creationListView;

    @FXML
    private void initialize() {
        // stuff
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // open PlaybackScreen
        changeScene(event, "../PlaybackScreen.fxml");
    }

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // ask for confirmation
        Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete the " +
                "selected creation?", ButtonType.YES, ButtonType.CANCEL); // add selected creation name here later
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            // delete creation file
        }
    }

    @FXML
    private void pressCreateButton(ActionEvent event) {
        // open WikitSearchScreen
        changeScene(event, "../WikitSearchScreen.fxml");
    }

    public String getCreationFileName() {
    	return "video.mp4";
    }
}
