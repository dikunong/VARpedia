package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Controller {

    public void changeScene(ActionEvent event, String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
