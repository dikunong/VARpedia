package varpedia.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;

public class PlaybackController extends Controller {

    @FXML
    private MediaView mediaView;
    @FXML
    private Button playBtn;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label timeLabel;
    @FXML
    private Label volLabel;
    @FXML
    private Slider volSlider;
    @FXML
    private Button exitBtn;

    private MediaPlayer _player;

    @FXML
    private void initialize() {
        // mediaView.setMediaPlayer(player);
        // playMedia(new File("video.mp4"));
        // it seems it's not possible to chuck parameters into initialize()
        // see VARpediaApp for an early idea on how to pass the video filename from MainController to PlaybackController

        // Tudor's code - adding event listeners???
        // particularly stuff regarding the sliders and MediaView sizing
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // Tudor's code - play.setOnAction()
    }

    @FXML
    private void pressExitButton(ActionEvent event) {
        // Tudor's code - stopMedia()
        changeScene(event, "../MainScreen.fxml");
    }

    // NOTE - missing @FXML methods for the time and volume sliders
    // I'm not sure which methods are the best equivalent to valueChangingProperty() and valueProperty()
    // let's figure this out in our meeting tomorrow

    public void playMedia(File file) {
        // Tudor's code - playMedia()
    }
}
