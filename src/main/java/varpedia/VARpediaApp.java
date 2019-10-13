package varpedia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import varpedia.tasks.ClearTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main class of VARpedia.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
public class VARpediaApp extends Application {

	/**
	 * Make a Threadpool that will terminate after 5 seconds so that the actual executable times out 5 seconds after the window is closed (at most).
	 * @return The new Threadpool
	 */
	public static ExecutorService newTimedCachedThreadPool() {
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}
	
    @Override
    public void start(Stage primaryStage) {
        try {
            Font.loadFont(VARpediaApp.class.getResource("fonts/Roboto-Regular.ttf").toExternalForm(),10);
            Font.loadFont(VARpediaApp.class.getResource("fonts/RobotoBold.ttf").toExternalForm(),10);
            Font.loadFont(VARpediaApp.class.getResource("fonts/Montserrat-Medium.ttf").toExternalForm(),10);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("MainScreen.fxml"));
            Parent layout = loader.load();
            primaryStage.setTitle("VARpedia");
            primaryStage.setScene(new Scene(layout));
            primaryStage.show();

            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(700);

            // if the application is abruptly closed, prompt before exiting
            // if the user chooses to exit, delete appfiles first
            primaryStage.setOnCloseRequest(e -> {
                AlertHelper.getInstance().showAlert(Alert.AlertType.WARNING,
                        "Do you want to exit? Any unsaved creation progress will be lost.",
                        ButtonType.YES, ButtonType.CANCEL);

                if (AlertHelper.getInstance().getResult() == ButtonType.YES) {
                    Task<Void> task = new ClearTask(new File("appfiles"));
                    task.run();
                    Platform.exit();
                    System.exit(0);
                } else {
                    e.consume();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that various app directories exist and aren't folders
        checkDir(new File("creations"));
        checkDir(new File("appfiles"));
        checkDir(new File("appfiles/audio"));

    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Checks if a given directory is a file or doesn't exist, creating a new directory if needed.
     * @param dir Directory to be checked
     */
    private void checkDir(File dir) {
        if (dir.isFile()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

}
