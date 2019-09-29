package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

    public String getDataFromFile(String filename) {
        String output = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("appfiles/" + filename), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                // if the message is a filename, don't add a line break or loop at all
                // this is obviously not the prettiest solution
                // TODO: improve this design
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
