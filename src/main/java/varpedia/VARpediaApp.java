package varpedia;

import javafx.application.Application;
/*import javafx.concurrent.Task;*/
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
/*import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;*/

public class VARpediaApp extends Application {

    // This is how Tudor managed threads in his A2
    // Would be good to use this method but currently can't use it as Controllers are static
    // and you can't reference a non-static method from a static context.
    // Aaaand we just set MainController to static for our Singleton approach attempt.
    // Figure this out later.

    /*private ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public void submit(Task<?> task) {
        pool.submit(task);
    }*/

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
    }

    public static void main(String[] args) {
        launch(args);
    }

}
