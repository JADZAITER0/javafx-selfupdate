package com.example.selfupdate.testjavafxmvci.views.videoPlayer;

import com.example.selfupdate.testjavafxmvci.views.ViewManager;
import com.example.selfupdate.testjavafxmvci.views.main.BaseApplicationModel;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoPlayerController {

    private static final Logger LOGGER = Logger.getLogger(VideoPlayerController.class.getName());

    @FXML private MediaView mediaView;
    @FXML private StackPane videoContainer;
    @FXML private Label statusLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private StackPane playPauseButton;
    @FXML private StackPane muteButton;
    @FXML private StackPane fullscreenButton;
    @FXML private VBox controlsContainer;

    private MediaPlayer mediaPlayer;
    private boolean isUpdatingSlider = false;
    private boolean isSeeking = false;
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private Stage stage;

    // Icons - created in code
    private FontIcon playPauseIcon;
    private FontIcon volumeIcon;
    private FontIcon fullscreenIcon;

    // Auto-hide controls
    private PauseTransition hideControlsTimer;
    private boolean isMouseOverControls = false;

    @FXML
    public void initialize() {
        initializeIcons();
        setupControlListeners();
        bindMediaViewSize();
        setupSliderStyles();
        setupAutoHideControls();

    }

    private void setupSliderStyles() {
        // Force JavaFX to show the filled track (progress) on sliders
        // This is needed because AtlantaFX might override default behavior
        timeSlider.setStyle(timeSlider.getStyle() + "-fx-show-tick-labels: false; -fx-show-tick-marks: false;");
        volumeSlider.setStyle(volumeSlider.getStyle() + "-fx-show-tick-labels: false; -fx-show-tick-marks: false;");

        // Wait for sliders to be laid out, then initialize progress
        Platform.runLater(() -> {
            updateSliderProgress(volumeSlider, volumeSlider.getValue(), volumeSlider.getValue());
        });
    }

    private void initializeIcons() {
        // Play/Pause icon
        playPauseIcon = new FontIcon("mdi2p-play");
        playPauseIcon.setIconSize(24);
        playPauseButton.getChildren().add(playPauseIcon);

        // Volume icon
        volumeIcon = new FontIcon("mdi2v-volume-high");
        volumeIcon.setIconSize(20);
        muteButton.getChildren().add(volumeIcon);

        // Fullscreen icon
        fullscreenIcon = new FontIcon("mdi2f-fullscreen");
        fullscreenIcon.setIconSize(24);
        fullscreenButton.getChildren().add(fullscreenIcon);
    }

    /**
     * Sets the video source and initializes the media player
     * @param videoUrl The URL or path to the video file
     */
    public void setVideoSource(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            showError("Invalid video URL");
            return;
        }

        // Cleanup existing media player
        disposeMediaPlayer();

        try {
            Media media = new Media(videoUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            setupMediaPlayer();
            showStatus("Loading video...", 2);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load video: " + videoUrl, e);
            showError("Failed to load video: " + e.getMessage());
        }
    }

    private void setupMediaPlayer() {
        if (mediaPlayer == null) return;

        // Handle media player ready state
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            timeSlider.setMax(totalDuration.toSeconds());
            totalTimeLabel.setText(formatTime(totalDuration));
            statusLabel.setVisible(false);
            updateSliderProgress(timeSlider, 0, 0);
            LOGGER.info("Media ready. Duration: " + formatTime(totalDuration));
        });

        // Update time slider and labels as video plays
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!isSeeking && !timeSlider.isValueChanging()) {
                isUpdatingSlider = true;
                double currentSeconds = newTime.toSeconds();
                double totalSeconds = mediaPlayer.getTotalDuration().toSeconds();
                timeSlider.setValue(currentSeconds);
                currentTimeLabel.setText(formatTime(newTime));

                // Calculate buffered percentage
                double bufferedSeconds = 0;
                Duration bufferTime = mediaPlayer.getBufferProgressTime();
                if (bufferTime != null && !bufferTime.isUnknown()) {
                    bufferedSeconds = bufferTime.toSeconds();
                }

                updateSliderProgress(timeSlider, currentSeconds / totalSeconds, bufferedSeconds / totalSeconds);
                isUpdatingSlider = false;
            }
        });

        // Update buffer progress
        mediaPlayer.bufferProgressTimeProperty().addListener((obs, oldBuffer, newBuffer) -> {
            if (!isSeeking && mediaPlayer.getTotalDuration() != null) {
                double totalSeconds = mediaPlayer.getTotalDuration().toSeconds();
                double currentSeconds = mediaPlayer.getCurrentTime().toSeconds();
                double bufferedSeconds = newBuffer != null && !newBuffer.isUnknown() ? newBuffer.toSeconds() : 0;
                updateSliderProgress(timeSlider, currentSeconds / totalSeconds, bufferedSeconds / totalSeconds);
            }
        });

        // Handle end of media
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.pause();
            isPlaying.set(false);
            updatePlayPauseIcon();
        });

        // Handle errors
        mediaPlayer.setOnError(() -> {
            String errorMsg = "Media error";
            if (mediaPlayer.getError() != null) {
                errorMsg += ": " + mediaPlayer.getError().getMessage();
            }
            LOGGER.log(Level.SEVERE, errorMsg, mediaPlayer.getError());
            showError(errorMsg);
        });

        // Handle stalled state
        mediaPlayer.setOnStalled(() -> showStatus("Buffering...", 0));

        // Set initial volume
        mediaPlayer.setVolume(volumeSlider.getValue());
    }

    private void updateSliderProgress(Slider slider, double percentage, double bufferedPercentage) {
        // Update the slider's progress visually using inline styles
        percentage = Math.max(0, Math.min(1, percentage));
        bufferedPercentage = Math.max(percentage, Math.min(1, bufferedPercentage));

        double playedPercent = percentage * 100;
        double bufferedPercent = bufferedPercentage * 100;

        // JavaFX uses a ProgressBar-like approach for sliders
        // We need to style the track background to show progress and buffer
        String baseStyle = slider.getId() != null && slider.getId().equals("timeSlider")
                ? "-fx-pref-height: 5px;"
                : "-fx-pref-height: 3px;";

        if (slider.lookup(".track") != null) {
            slider.lookup(".track").setStyle(
                    baseStyle +
                            "-fx-background-color: linear-gradient(to right, " +
                            "-color-accent-emphasis 0%, " +
                            "-color-accent-emphasis " + playedPercent + "%, " +
                            "-color-accent-subtle " + playedPercent + "%, " +
                            "-color-accent-subtle " + bufferedPercent + "%, " +
                            "-color-border-default " + bufferedPercent + "%, " +
                            "-color-border-default 100%);"
            );
        }
    }

    private void setupControlListeners() {
        // Play/Pause button
        playPauseButton.setOnMouseClicked(e -> togglePlayPause());

        // Time slider - seeking
        timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && mediaPlayer != null) {
                // User finished dragging, seek to the position
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                isSeeking = false;
            } else if (isChanging) {
                // User started dragging
                isSeeking = true;
            }
        });

        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingSlider && isSeeking && mediaPlayer != null) {
                // Update time label while dragging
                currentTimeLabel.setText(formatTime(Duration.seconds(newVal.doubleValue())));
            }
        });

        // Allow clicking on timeline to seek
        timeSlider.setOnMouseClicked(e -> {
            if (mediaPlayer != null && !timeSlider.isDisabled()) {
                double mouseX = e.getX();
                double sliderWidth = timeSlider.getWidth();
                double percent = mouseX / sliderWidth;
                double newValue = percent * timeSlider.getMax();
                timeSlider.setValue(newValue);
                mediaPlayer.seek(Duration.seconds(newValue));
            }
        });

        // Volume slider
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                double volume = newVal.doubleValue();
                mediaPlayer.setVolume(volume);
                updateVolumeIcon(volume);
                updateSliderProgress(volumeSlider, volume, volume);

                // Unmute if volume is increased from muted state
                if (volume > 0 && mediaPlayer.isMute()) {
                    mediaPlayer.setMute(false);
                }
            }
        });

        // Mute button
        muteButton.setOnMouseClicked(e -> toggleMute());

        // Fullscreen button
        fullscreenButton.setOnMouseClicked(e -> toggleFullscreen());

        // Update volume icon initially
        updateVolumeIcon(volumeSlider.getValue());
    }

    private void bindMediaViewSize() {
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty());

        // Force rebind when entering/exiting fullscreen to fix sizing issues
        if (videoContainer.getScene() != null && videoContainer.getScene().getWindow() != null) {
            Stage currentStage = (Stage) videoContainer.getScene().getWindow();
            currentStage.fullScreenProperty().addListener((obs, wasFullscreen, isFullscreen) -> {
                // Unbind and rebind to force layout recalculation
                Platform.runLater(() -> {
                    mediaView.fitWidthProperty().unbind();
                    mediaView.fitHeightProperty().unbind();
                    mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                    mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
                });
            });
        }
    }

    private void setupAutoHideControls() {
        if (controlsContainer == null) {
            LOGGER.warning("controlsContainer is null - auto-hide will not work");
            return;
        }

        // Initialize hide timer (3 seconds of inactivity)
        hideControlsTimer = new PauseTransition(Duration.seconds(3));
        hideControlsTimer.setOnFinished(e -> {
            LOGGER.info("Hide timer finished, hiding controls");
            hideControls();
        });

        // Track mouse over controls
        controlsContainer.setOnMouseEntered(e -> {
            isMouseOverControls = true;
            LOGGER.info("Mouse entered controls");
            showControls();
            hideControlsTimer.stop();
        });

        controlsContainer.setOnMouseExited(e -> {
            isMouseOverControls = false;
            LOGGER.info("Mouse exited controls");
            if (isPlaying.get()) {
                resetHideTimer();
            }
        });

        // Track mouse movement in video container
        videoContainer.setOnMouseMoved(e -> {
            showControls();
            if (!isMouseOverControls && isPlaying.get()) {
                resetHideTimer();
            }
        });

        // Initial state - controls visible
        controlsContainer.setOpacity(1.0);
        controlsContainer.setVisible(true);
        controlsContainer.setMouseTransparent(false);
    }

    private void showControls() {
        if (controlsContainer == null) return;

        // Cancel any ongoing hide timer
        if (hideControlsTimer != null) {
            hideControlsTimer.stop();
        }

        // Only animate if not already visible
        if (controlsContainer.getOpacity() < 1.0) {
            LOGGER.info("Showing controls with fade in");
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), controlsContainer);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }

        controlsContainer.setVisible(true);
        controlsContainer.setMouseTransparent(false);
    }

    private void hideControls() {
        if (controlsContainer == null || isMouseOverControls) {
            LOGGER.info("Not hiding - mouse over controls or null container");
            return;
        }

        // Only hide if video is playing
        if (!isPlaying.get()) {
            LOGGER.info("Not hiding - video is paused");
            return;
        }

        LOGGER.info("Hiding controls with fade out");

        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), controlsContainer);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (controlsContainer.getOpacity() == 0.0) {
                controlsContainer.setMouseTransparent(true);
                LOGGER.info("Controls hidden and mouse transparent");
            }
        });
        fadeOut.play();
    }

    private void resetHideTimer() {
        if (hideControlsTimer != null && isPlaying.get()) {
            LOGGER.info("Resetting hide timer");
            hideControlsTimer.stop();
            hideControlsTimer.playFromStart();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            showError("No media loaded");
            return;
        }

        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            isPlaying.set(false);
            showControls(); // Keep controls visible when paused
            hideControlsTimer.stop();
        } else if (status == MediaPlayer.Status.PAUSED ||
                status == MediaPlayer.Status.READY ||
                status == MediaPlayer.Status.STOPPED) {
            mediaPlayer.play();
            isPlaying.set(true);
            resetHideTimer(); // Start auto-hide when playing
        }

        updatePlayPauseIcon();
    }

    private void updatePlayPauseIcon() {
        if (isPlaying.get()) {
            playPauseIcon.setIconLiteral("mdi2p-pause");
        } else {
            playPauseIcon.setIconLiteral("mdi2p-play");
        }
    }

    private void toggleMute() {
        if (mediaPlayer == null) return;

        boolean isMuted = mediaPlayer.isMute();
        mediaPlayer.setMute(!isMuted);

        if (!isMuted) {
            volumeIcon.setIconLiteral("mdi2v-volume-off");
        } else {
            updateVolumeIcon(mediaPlayer.getVolume());
        }
    }

    private void updateVolumeIcon(double volume) {
        if (mediaPlayer != null && mediaPlayer.isMute()) {
            volumeIcon.setIconLiteral("mdi2v-volume-off");
        } else if (volume == 0) {
            volumeIcon.setIconLiteral("mdi2v-volume-mute");
        } else if (volume < 0.5) {
            volumeIcon.setIconLiteral("mdi2v-volume-medium");
        } else {
            volumeIcon.setIconLiteral("mdi2v-volume-high");
        }
    }

    //TODO handle escape key to exit fullscreen
    private void toggleFullscreen() {
        if (stage == null) {
            // Try to get the stage from the scene
            if (videoContainer.getScene() != null && videoContainer.getScene().getWindow() != null) {
                stage = (Stage) videoContainer.getScene().getWindow();
            }
        }

        if (stage != null) {



            boolean isCurrentlyFullscreen = stage.isFullScreen();

            if (!isCurrentlyFullscreen) {
                // Entering fullscreen
                stage.setFullScreen(true);
                BaseApplicationModel.getInstance().setIsFullscreen(true);
                fullscreenIcon.setIconLiteral("mdi2f-fullscreen-exit");
            } else {
                // Exiting fullscreen
                stage.setFullScreen(false);
                BaseApplicationModel.getInstance().setIsFullscreen(false);
                fullscreenIcon.setIconLiteral("mdi2f-fullscreen");
            }
        } else {
            LOGGER.warning("Cannot toggle fullscreen: Stage is not available");
        }
    }

    /**
     * Set the stage reference manually if needed
     * Call this after the scene is shown
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }

        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private void showStatus(String message, int seconds) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);

            if (seconds > 0) {
                PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
                pause.setOnFinished(e -> {
                    statusLabel.setVisible(false);
                    statusLabel.setManaged(false);
                });
                pause.play();
            }
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.getStyleClass().add("error");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        });
    }

    /**
     * Public methods for external control
     */
    public void play() {
        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
            isPlaying.set(true);
            updatePlayPauseIcon();
            resetHideTimer();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            isPlaying.set(false);
            updatePlayPauseIcon();
            showControls();
            if (hideControlsTimer != null) {
                hideControlsTimer.stop();
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying.set(false);
            updatePlayPauseIcon();
            showControls();
            if (hideControlsTimer != null) {
                hideControlsTimer.stop();
            }
        }
    }

    public void seek(Duration duration) {
        if (mediaPlayer != null && duration != null) {
            mediaPlayer.seek(duration);
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            double clampedVolume = Math.max(0.0, Math.min(1.0, volume));
            volumeSlider.setValue(clampedVolume);
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isPlaying() {
        return isPlaying.get();
    }

    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    /**
     * Cleanup method - MUST be called when closing/disposing the player
     * This prevents memory leaks
     */
    public void dispose() {
        if (hideControlsTimer != null) {
            hideControlsTimer.stop();
        }
        disposeMediaPlayer();
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                LOGGER.info("Media player disposed");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error disposing media player", e);
            } finally {
                mediaPlayer = null;
            }
        }
    }
}