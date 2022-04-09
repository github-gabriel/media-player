package de.mediaplayer.mediaplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;

import static javax.swing.JOptionPane.*;

public class Window extends Application {

    private Stage stage;
    private Label playTime, volume;
    private MediaPlayer mediaPlayer;

    private final boolean repeat = false;

    private Slider slider, volumeSlider;

    private Button btnPlay;

    private Duration duration;

    private boolean atEndOfMedia = false;

    public Window() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Media File");
        File file = fileChooser.showOpenDialog( null );
        String filePath = file.toURI().toString();
        initalize(filePath);
    }

    private void initalize(String mediaURL) {

        stage = new Stage();
        stage.setTitle("Java Media Player");
        BorderPane borderPane = new BorderPane();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Media media = new Media(mediaURL);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setOnReady(() -> stage.sizeToScene());
        MediaView mediaView = new MediaView(mediaPlayer);

        borderPane.setCenter(mediaView);

        // Button

        btnPlay = new Button(">");
        btnPlay.setPrefSize(32, 32);
        btnPlay.setMinSize(32, 32);

        btnPlay.setOnAction(arg0 -> {
            Status status = mediaPlayer.getStatus();

            if (status == Status.UNKNOWN || status == Status.HALTED) {
                return;
            }

            if (status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) {
                if (atEndOfMedia) {
                    mediaPlayer.seek(mediaPlayer.getStartTime());
                    atEndOfMedia = false;
                }
                btnPlay.setText(">");
                mediaPlayer.play();
            } else {
                btnPlay.setText("||");
                mediaPlayer.pause();
            }
        });

        // Label

        playTime = new Label();
        playTime.setPrefWidth(100);
        playTime.setMinWidth(100);

        // Slider

        volumeSlider = new Slider();
        volumeSlider.setMinWidth(150);

        volumeSlider.valueProperty().addListener(ov -> {
            if (volumeSlider.isValueChanging()) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        volume = new Label();
        volume.setPrefWidth(34);
        volume.setMinWidth(34);

        slider = new Slider();
        slider.prefWidthProperty().bind(borderPane.widthProperty());

        slider.valueProperty().addListener(ov -> {
            if (slider.isValueChanging()) {
                mediaPlayer.seek(duration.multiply(slider.getValue() / 100.0));
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(6);
        hbox.setPadding(new Insets(75, 0, 30, 8));

        mediaPlayer.currentTimeProperty().addListener(ov -> updateValues());

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            updateValues();
        });

        mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mediaPlayer.setOnEndOfMedia(() -> {
            if (!repeat) {
                btnPlay.setText(">");
                atEndOfMedia = true;
            }
        });

        slider.setMinWidth(50);
        slider.setMaxWidth(Double.MAX_VALUE);
        hbox.getChildren().addAll(btnPlay, volumeSlider, volume, slider, playTime);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setMinHeight(90);
        hbox.setPrefHeight(90);
        borderPane.setBottom(hbox);
        borderPane.setMinHeight(0);

        Scene scene = new Scene(borderPane);

        DoubleProperty mvw = mediaView.fitWidthProperty();
        DoubleProperty mvh = mediaView.fitHeightProperty();
        mvw.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        mvh.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        mediaView.setPreserveRatio(true);

        borderPane.setPrefSize(1280, 720);
        mediaView.setPreserveRatio(true);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.show();

    }

    protected void updateValues() {
        if (playTime != null && slider != null && volumeSlider != null) {
            Platform.runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                playTime.setText(formatTime(currentTime, duration));
                volume.setText(String.format("%.0f", volumeSlider.getValue()) + "%");
                slider.setDisable(duration.isUnknown());
                if (!slider.isDisabled() && duration.greaterThan(Duration.ZERO) && !slider.isValueChanging()) {
                    slider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
                if (!volumeSlider.isValueChanging()) {
                    volumeSlider.setValue((int) Math.round(mediaPlayer.getVolume() * 100));
                }
            });
        }
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
            }
        }
    }

    @Override
    public void start(Stage arg0) {

    }
}

