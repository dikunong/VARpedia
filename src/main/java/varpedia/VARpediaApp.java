package varpedia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import varpedia.controllers.MainController;

import java.io.IOException;

public class VARpediaApp extends Application {

    private MainController _mainController;

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

            // an early attempt at something that will allow passing the video name from MainController to PlaybackController
            _mainController = loader.getController();
            /* hypothetically, you could now add a getter method to this class
            and then do string = VARpediaApp.getMainController().getCreationFileName(); in PlaybackController
            or something like that. there must be a better solution though...
            https://coderanch.com/t/701958/java/Passing-Data-Controller-Controller-JavaFx
            */

            // just had a Big Brain moment - we might well be passing data between Controllers multiple times
            // e.g. entering the name of the creation on one screen --> making the creation the next screen
            // hmm there must be a better way

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
