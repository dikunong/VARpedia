package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Abstract Controller class that provides scene-changing and temporary data storage/retrieval functionality
 * to all Controllers.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
public abstract class Controller {

    /**
     * Switches the FXML root to the given window.
     * @param event The event causing the change (used to retrieve the JavaFX stage)
     * @param fxml The FXML window to switch to
     */
    public void changeScene(ActionEvent event, String fxml) {
        try {
        	Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        	Scene currentScene = ((Node) event.getSource()).getScene();
        	currentScene.setRoot(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new scene and sets it as a "dialog" on top of the main application with modal focus.
     * @param event The event causing the change (used to retrieve the JavaFX stage)
     * @param fxml The FXML window to create
     * @param title The title for the new dialog
     */
    protected void createDialog(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);

            dialog.initOwner(parentStage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes a given dialog's stage.
     * @param event The event causing the change (used to retrieve the JavaFX stage)
     */
    protected void closeDialog(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Stores a string in a file, typically to send data between Controllers.
     * @param msg String to be stored
     * @param filename File to store message in
     */
    public void sendDataToFile(String msg, String filename) {
        try {
            File file = new File("appfiles/" + filename);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writer.write(msg);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a string from a file, typically to receive data between Controllers.
     * @param filename File to retrieve message from
     * @return String that was stored
     */
    public String getDataFromFile(String filename) {
        String output = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("appfiles/" + filename), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                // if the message contains a filename, don't add a line break or loop at all
                if (filename.equals("playback-name.txt") || filename.equals("search-term.txt") || filename.equals("selected-chunks.txt") || filename.equals("image-count.txt")) {
                    break;
                }
                sb.append(System.lineSeparator());
                line = reader.readLine();
            }
            output = sb.toString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

}
