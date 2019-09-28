package varpedia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class VARpediaApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("MainScreen.fxml"));
            Parent layout = loader.load();
            primaryStage.setTitle("VARpedia");
            primaryStage.setScene(new Scene(layout));
            primaryStage.show();

            primaryStage.setMinHeight(200);
            primaryStage.setMinWidth(520);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File creationsDir = new File("creations");
        File appfilesDir = new File("appfiles");

        // ensure the creations directory is actually a directory AND exists
        if (creationsDir.isFile()) {
            creationsDir.delete();
        }
        if (!creationsDir.exists()) {
            creationsDir.mkdir();
        }
        // ensure the appfiles directory is actually a directory AND exists
        if (appfilesDir.isFile()) {
            appfilesDir.delete();
        }
        if (!appfilesDir.exists()) {
            appfilesDir.mkdir();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
