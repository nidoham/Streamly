package org.schabi.newpipe;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.TimeBar;

import org.schabi.newpipe.databinding.ActivityPlayerBinding;

import java.util.concurrent.TimeUnit;

/**
 * PlayerActivity class: Video player activity for the Streamly app.
 */
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    private static final String INTENT_VIDEO_URL = "video_url";
    private static final String INTENT_VIDEO_TITLE = "video_title";
    private static final int CONTROL_HIDE_TIMEOUT = 3500;

    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private GestureDetectorCompat gestureDetector;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable;
    private AudioManager audioManager;
    private int maxVolume;
    private float currentBrightness;
    private boolean controlsVisible = true;
    private boolean isTV;
    private String videoUrl;
    private String videoTitle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isTV = getPackageManager().hasSystemFeature("android.software.leanback");
        handleIntent();
        initializeAudioManager();
        initializeBrightness();
        initializeHideControlsRunnable();
        initializePlayer();
        setupUI();

        if (!isTV) {
            setupGestureDetector();
        }
        startControlsTimer();
    }

    private void handleIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            videoUrl = intent.getStringExtra(INTENT_VIDEO_URL);
            videoTitle = intent.getStringExtra(INTENT_VIDEO_TITLE);
            if (videoTitle == null) {
                videoTitle = getString(R.string.video);
            }
        } else {
            videoUrl = "";
            videoTitle = getString(R.string.video);
        }
        binding.videoTitle.setText(videoTitle);
    }

    private void initializeAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initializeBrightness() {
        try {
            final int brightness = Settings.System.getInt(
                    getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            currentBrightness = brightness / 255.0f * 100;
        } catch (final Settings.SettingNotFoundException e) {
            currentBrightness = 50;
        }
    }

    private void initializeHideControlsRunnable() {
        hideControlsRunnable = this::hideControls;
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        if (videoUrl != null && !videoUrl.isEmpty()) {
            final MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
        }

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(final int state) {
                if (state == Player.STATE_READY) {
                    updateTimers();
                    updatePlayPauseButton();
                    binding.loadingOverlay.setVisibility(View.GONE);
                } else if (state == Player.STATE_BUFFERING) {
                    binding.loadingOverlay.setVisibility(View.VISIBLE);
                } else {
                    binding.loadingOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onIsPlayingChanged(final boolean isPlaying) {
                updatePlayPauseButton();
            }
        });
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPlayPause.setOnClickListener(v -> togglePlayPause());

        final TimeBar timeBar = binding.timeBar;
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(final TimeBar timeBar, final long position) {
                binding.seekPreviewContainer.setVisibility(View.VISIBLE);
                binding.seekPreviewTime.setText(formatDuration(position));
                handler.removeCallbacks(hideControlsRunnable);
            }

            @Override
            public void onScrubMove(final TimeBar timeBar, final long position) {
                binding.seekPreviewTime.setText(formatDuration(position));
            }

            @Override
            public void onScrubStop(final TimeBar timeBar, final long position,
                                    final boolean canceled) {
                binding.seekPreviewContainer.setVisibility(View.GONE);
                if (!canceled) {
                    player.seekTo(position);
                }
                startControlsTimer();
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(final MotionEvent e) {
                        toggleControlsVisibility();
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(final MotionEvent e) {
                        final int screenWidth = getResources()
                                .getDisplayMetrics().widthPixels;
                        if (e.getX() < screenWidth / 2) {
                            rewind();
                            showRewindOverlay();
                        } else {
                            fastForward();
                            showFastForwardOverlay();
                        }
                        return true;
                    }

                    @Override
                    public boolean onScroll(final MotionEvent e1,
                                            final MotionEvent e2,
                                            final float distanceX,
                                            final float distanceY) {
                        if (e1 == null) {
                            return false;
                        }
                        final int screenWidth = getResources()
                                .getDisplayMetrics().widthPixels;
                        final boolean isVerticalScroll = Math.abs(distanceY)
                                > Math.abs(distanceX);
                        if (isVerticalScroll) {
                            if (e1.getX() < screenWidth / 2) {
                                handleBrightnessControl(-distanceY);
                            } else {
                                handleVolumeControl(-distanceY);
                            }
                        }
                        return true;
                    }
                });

        binding.gestureOverlay.setOnTouchListener((v, event) ->
                gestureDetector.onTouchEvent(event));
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
        updatePlayPauseButton();
        startControlsTimer();
    }

    private void updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(player.isPlaying()
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);
    }

    private void toggleControlsVisibility() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        binding.topBar.setVisibility(View.VISIBLE);
        binding.bottomControls.setVisibility(View.VISIBLE);
        controlsVisible = true;
        startControlsTimer();
    }

    private void hideControls() {
        binding.topBar.setVisibility(View.GONE);
        binding.bottomControls.setVisibility(View.GONE);
        controlsVisible = false;
        handler.removeCallbacks(hideControlsRunnable);
    }

    private void startControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable);
        if (!isTV) {
            handler.postDelayed(hideControlsRunnable, CONTROL_HIDE_TIMEOUT);
        }
    }

    private void rewind() {
        player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
        startControlsTimer();
    }

    private void fastForward() {
        player.seekTo(Math.min(player.getDuration(),
                player.getCurrentPosition() + 10000));
        startControlsTimer();
    }

    private void showRewindOverlay() {
        binding.doubleTapOverlayLeft.setVisibility(View.VISIBLE);
        handler.postDelayed(() ->
                binding.doubleTapOverlayLeft.setVisibility(View.GONE), 800);
    }

    private void showFastForwardOverlay() {
        binding.doubleTapOverlayRight.setVisibility(View.VISIBLE);
        handler.postDelayed(() ->
                binding.doubleTapOverlayRight.setVisibility(View.GONE), 800);
    }

    private void handleBrightnessControl(final float delta) {
        binding.brightnessControlContainer.setVisibility(View.VISIBLE);
        binding.brightnessSeekbar.setVisibility(View.VISIBLE);
        final float change = delta * 0.1f;
        currentBrightness = Math.max(0, Math.min(100, currentBrightness + change));
        binding.brightnessSeekbar.setProgress((int) currentBrightness);
        binding.brightnessPercentage.setText((int) currentBrightness + "%");
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = currentBrightness / 100.0f;
        getWindow().setAttributes(params);
        handler.removeCallbacks(this::hideBrightnessControl);
        handler.postDelayed(this::hideBrightnessControl, 2000);
    }

    private void hideBrightnessControl() {
        binding.brightnessControlContainer.setVisibility(View.GONE);
    }

    private void handleVolumeControl(final float delta) {
        binding.volumeControlContainer.setVisibility(View.VISIBLE);
        binding.volumeSeekbar.setVisibility(View.VISIBLE);
        final int currentVolume = audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        int volumePercent = (currentVolume * 100) / maxVolume;
        final float change = delta * 0.1f;
        volumePercent = Math.max(0, Math.min(100,
                volumePercent + (int) (change * 2)));
        final int newVolume = (volumePercent * maxVolume) / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        binding.volumeSeekbar.setProgress(volumePercent);
        binding.volumePercentage.setText(volumePercent + "%");
        binding.volumeIcon.setImageResource(volumePercent == 0
                ? android.R.drawable.ic_lock_silent_mode
                : android.R.drawable.ic_lock_silent_mode_off);
        handler.removeCallbacks(this::hideVolumeControl);
        handler.postDelayed(this::hideVolumeControl, 2000);
    }

    private void hideVolumeControl() {
        binding.volumeControlContainer.setVisibility(View.GONE);
    }

    private void updateTimers() {
        final long duration = player.getDuration();
        binding.tvTotalTime.setText(formatDuration(duration));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    binding.tvCurrentTime.setText(
                            formatDuration(player.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private String formatDuration(final long durationMs) {
        final long hours = TimeUnit.MILLISECONDS.toHours(durationMs);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (isTV) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if (event.getRepeatCount() == 0
                            && binding.btnPlayPause.isFocused()) {
                        togglePlayPause();
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    togglePlayPause();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    fastForward();
                    showFastForwardOverlay();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    rewind();
                    showRewindOverlay();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    if (controlsVisible) {
                        hideControls();
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
