package varpedia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VARpediaApp extends Application {

	public static ExecutorService newTimedCachedThreadPool() {
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}
	
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
