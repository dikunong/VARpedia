package varpedia.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class MainScreenController extends Controller {

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
    private void pressPlayButton (ActionEvent event) {
        // open PlaybackScreen
    }

    @FXML
    private void pressDeleteButton (ActionEvent event) {
        // ask for confirmation
        // delete creation file
    }

    @FXML
    private void pressCreateButton (ActionEvent event) {
        // open WikitSearchScreen
        changeScene(event, "../WikitSearchScreen.fxml");
    }
}
