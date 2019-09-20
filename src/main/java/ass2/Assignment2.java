package ass2;

import java.io.File;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Assignment2 extends Application {
	private Stage theStage;
	private Scene playerScene;
	private Pane viewPane;
	private MediaView mediaView;
	private Slider timeSlider;
	private Label timeLabel;
	private Label volLabel;
	
	private MediaPlayer player;
	private Duration duration;
	private boolean actualPaused;
	
	private ChangeListener<Number> timeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
		player.seek(duration.multiply(newValue.doubleValue() / 100));
	};
	
	public void playMedia(File file) {
		Media media = new Media(file.toURI().toString());
		player = new MediaPlayer(media);
		player.setAutoPlay(true);
		
		player.setOnReady(() -> {
			duration = media.getDuration();
			timeSlider.setDisable(false);
			double oldHeight = viewPane.getHeight();
			viewPane.resize(viewPane.getWidth(), 0);
			viewPane.resize(viewPane.getWidth(), oldHeight);
		});
		
		player.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
			if (duration != null) {
				timeSlider.valueProperty().removeListener(timeListener);
				timeSlider.setValue((player.getCurrentTime().toMillis() / duration.toMillis()) * 100);
				timeSlider.valueProperty().addListener(timeListener);

				int count = (int)player.getCurrentTime().toSeconds();
				int sec = count % 60;
				count /= 60;
				int min = count % 60;
				count /= 60;
				int hr = count;
				count = (int)duration.toSeconds();
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

		player.volumeProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			volLabel.setText("Vol: " + Math.round(player.getVolume() * 100) + "%");
		});
		
		mediaView.setMediaPlayer(player);
		theStage.setScene(playerScene);
	}
	
	public void stopMedia()
	{
		player.stop();
		player.dispose();
		player = null;
		mediaView.setMediaPlayer(null);
		timeSlider.setDisable(true);
	}
	
	private void createPlayerScene() throws Exception {
		//Elements
		mediaView = new MediaView();
		viewPane = new Pane();
		Button play = new Button("||");
		timeSlider = new Slider();
		timeLabel = new Label("0:00:00/0:00:00");
		volLabel = new Label("Vol: 100%");
		Slider volSlider = new Slider();
		HBox strip = new HBox(play, timeSlider, timeLabel, volLabel, volSlider);
		BorderPane pane = new BorderPane();
		playerScene = new Scene(pane, 1000, 700);
		
		//Layout
		viewPane.getChildren().add(mediaView);
		HBox.setHgrow(timeSlider, Priority.ALWAYS);
		play.setMinWidth(50);
		play.setMaxWidth(50);
		timeLabel.setMinWidth(60);
		timeLabel.setMaxWidth(60);
		pane.setCenter(viewPane);
		pane.setBottom(strip);
		
		viewPane.layoutBoundsProperty().addListener((Observable arg0) -> {
			Bounds newBounds = viewPane.getLayoutBounds();
			mediaView.setFitWidth(newBounds.getWidth());
			mediaView.setFitHeight(newBounds.getHeight());
			Bounds bounds = mediaView.getLayoutBounds();
			mediaView.setLayoutX((newBounds.getWidth() - bounds.getWidth()) / 2);
			mediaView.setLayoutY((newBounds.getHeight() - bounds.getHeight()) / 2);
		});
		
		//Actions
		timeSlider.valueProperty().addListener(timeListener);
		timeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {timeSlider.setValueChanging(true);});
		timeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent e) -> {timeSlider.setValueChanging(false);});
		timeSlider.setDisable(true);
		volSlider.setValue(100);
		
		play.setOnAction((ActionEvent event) -> {
			if (player.getStatus() == Status.PLAYING) {
				player.pause();
				play.setText("|>");
				actualPaused = true;
			} else {
				player.play();
				play.setText("||");
				actualPaused = false;
			}
		});

		timeSlider.valueChangingProperty().addListener((ObservableValue<? extends Boolean> obersvable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				player.pause();
			} else if (!actualPaused) {
				player.play();
			}
		});

		volSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			player.setVolume(newValue.doubleValue() / 100);
		});
	}
	
	@Override
	public void start(Stage stage) throws Exception {	
		theStage = stage;
		stage.setTitle("Simple media player");
		stage.setResizable(true);
		createPlayerScene();
		playMedia(new File("video.mp4"));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
