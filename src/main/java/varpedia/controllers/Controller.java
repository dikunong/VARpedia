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

    public void sendDataToFile(String msg) {
        try {
            File file = new File("appfiles/message.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(msg);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFromFile() {
        String output = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("appfiles/message.txt"));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                // this breaks single-line files from being read correctly. need a solution
                // potentially if(filename) { read one line } else { read line-by-line }
                // sb.append(System.lineSeparator());
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
