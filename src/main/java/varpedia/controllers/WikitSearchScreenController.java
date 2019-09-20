package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class WikitSearchScreenController {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button cancelBtn;

    @FXML
    private void initialize() {
        // stuff
    }

    @FXML
    private void pressSearchButton (ActionEvent event) {
        // perform Wikit search
        // display loading icon during search?
        // open TextEditorScreen
    }

    @FXML
    private void pressCancelButton (ActionEvent event) {
        // discard all existing temp files etc
        // open MainScreen
    }
}
