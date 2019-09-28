package varpedia.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import varpedia.tasks.WikitSearchTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WikitSearchController extends Controller {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button cancelBtn;

    private ExecutorService pool = Executors.newCachedThreadPool();

    @FXML
    private void initialize() {
        // stuff
    }

    @FXML
    private void pressSearchButton(ActionEvent event) {
        // check if there is VALID text in the text field
        String searchTerm = searchTextField.getText();
        if (searchTerm.equals("") /* || matches regex for incorrect terms */) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in a valid search term.");
            alert.showAndWait();
            return;
        }
        // perform Wikit search
        Task<Void> task = new WikitSearchTask(searchTerm);

        // apparently this is naughty
        // correct way to do it is to have Task return Boolean true or false?
        /*task.setOnFailed(event2 -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No valid Wikipedia articles found.");
            alert.showAndWait();
            return;
        });*/
        task.setOnSucceeded(event3 -> {
            // open TextEditorScreen
            changeScene(event, "/varpedia/TextEditorScreen.fxml");
        });

        pool.submit(task);
        // display loading icon during search?
    }

    @FXML
    private void pressCancelButton(ActionEvent event) {
        // discard all existing temp files etc
        // open MainScreen
        changeScene(event, "/varpedia/MainScreen.fxml");
    }
}
