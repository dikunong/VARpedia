package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public abstract class Controller {

    public void changeScene(ActionEvent event, String fxml) {
        try {
        	if (fxml.startsWith("..")) {
        		//TODO: Delete this
        		//So these calls might have been auto-generated, which is weird, since they don't seem to work in jar.
        		//Fix them up to do The Right Thing
        		fxml = "/varpedia" + fxml.substring(2);
        	}
        	
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(msg);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFromFile(String filename) {
        String output = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("appfiles/" + filename));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                // if the message is a filename, don't add a line break or loop at all
                if (filename.equals("playback-name.txt")) {
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
