package varpedia.controllers;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

import java.io.File;

/**
 * Controller for the PlaybackScreen, which plays creations.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
public class PlaybackController extends Controller {

    @FXML
    private MediaView mediaView;
    @FXML
    private Pane mediaPane;
    @FXML
    private Button playPauseBtn;
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
    private Duration _duration;
	private boolean _actualPaused;
    
	private ChangeListener<Number> _timeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
		_player.seek(_duration.multiply(newValue.doubleValue() / 100));
	};
    
    @FXML
    private void initialize() {
    	//Ensure media player fits in scene.
        mediaPane.layoutBoundsProperty().addListener((Observable arg0) -> {
			Bounds newBounds = mediaPane.getLayoutBounds();
			mediaView.setFitWidth(newBounds.getWidth());
			mediaView.setFitHeight(newBounds.getHeight());
			Bounds bounds = mediaView.getLayoutBounds();
			mediaView.setLayoutX((newBounds.getWidth() - bounds.getWidth()) / 2);
			mediaView.setLayoutY((newBounds.getHeight() - bounds.getHeight()) / 2);
		});
    	
        //Make the slider work properly
		timeSlider.valueProperty().addListener(_timeListener);
		timeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {timeSlider.setValueChanging(true);});
		timeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent e) -> {timeSlider.setValueChanging(false);});
		
		timeSlider.setDisable(true);
		volSlider.setValue(100);
    	
    	timeSlider.valueChangingProperty().addListener((ObservableValue<? extends Boolean> obersvable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				_player.pause();
			} else if (!_actualPaused) {
				_player.play();
			}
		});

		volSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			_player.setVolume(newValue.doubleValue() / 100);
		});
		
		//Play the selected media
		playMedia(new File(getDataFromFile("playback-name.txt")));
    }

    @FXML
    private void pressPlayPauseButton(ActionEvent event) {
		if (_player.getStatus() == Status.PLAYING) {
			_player.pause();
			playPauseBtn.setStyle("-fx-background-image: url(\"/varpedia/images/play_circle_white.png\")");
			_actualPaused = true;
		} else {
			_player.play();
			playPauseBtn.setStyle("-fx-background-image: url(\"/varpedia/images/pause_circle_white.png\")");
			_actualPaused = false;
		}
    }

    @FXML
    private void pressExitButton(ActionEvent event) {
		_player.stop();
		_player.dispose();
		_player = null;
		mediaView.setMediaPlayer(null);
		timeSlider.setDisable(true);
		createDialog(event, "/varpedia/RatingDialog.fxml", "Rate Your Confidence");
        changeScene(event, "/varpedia/MainScreen.fxml");
    }

    public void playMedia(File file) {
		Media media = new Media(file.toURI().toString());
		MediaPlayer player = new MediaPlayer(media);
		_player = player;
		_player.setAutoPlay(true);
		
		_player.setOnReady(() -> {
			//Enable the media player
			_duration = media.getDuration();
			timeSlider.setDisable(false);
			
			//Do some jiggling to get the media player at the right size.
			double oldHeight = mediaPane.getHeight();
			mediaPane.resize(mediaPane.getWidth(), 0);
			mediaPane.resize(mediaPane.getWidth(), oldHeight);
		});
		
		_player.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
			if (_duration != null) {
				//Update the duration text
				timeSlider.valueProperty().removeListener(_timeListener);
				timeSlider.setValue((player.getCurrentTime().toMillis() / _duration.toMillis()) * 100);
				timeSlider.valueProperty().addListener(_timeListener);

				int count = (int)player.getCurrentTime().toSeconds();
				int sec = count % 60;
				count /= 60;
				int min = count % 60;
				count /= 60;
				int hr = count;
				count = (int)_duration.toSeconds();
				int durSec = count % 60;
				count /= 60;
				int durMin = count % 60;
				count /= 60;
				int durHr = count;
				
				if (durHr == 0) {
					timeLabel.setText(String.format("%d:%02d/%d:%02d", min, sec, durMin, durSec));
				} else {
					timeLabel.setText(String.format("%d:%02d:%02d/%d:%02d:%02d", hr, min, sec, durHr, durMin, durSec));
				}
			}
		});

		_player.volumeProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			volLabel.setText("Vol: " + Math.round(player.getVolume() * 100) + "%");
		});
		
		mediaView.setMediaPlayer(_player);
	}
}
