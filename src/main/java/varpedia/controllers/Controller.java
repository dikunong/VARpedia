package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

/**
 * Abstract Controller class that provides scene-changing and temporary data storage/retrieval functionality
 * to all Controllers.
 */
public abstract class Controller {

    /**
     * Switches FXML scenes to the given window.
     * @param event The event causing the change (used to retrieve the JavaFX stage)
     * @param fxml The FXML window to switch to
     */
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

    /**
     * Stores a string in a file, typically to send data between Controllers.
     * @param msg String to be stored
     * @param filename File to store message in
     */
    public void sendDataToFile(String msg, String filename) {
        try {
            File file = new File("appfiles/" + filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
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
            BufferedReader reader = new BufferedReader(new FileReader("appfiles/" + filename));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                // if the message contains a filename, don't add a line break or loop at all
                if (filename.equals("playback-name.txt") || filename.equals("search-term.txt")) {
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
