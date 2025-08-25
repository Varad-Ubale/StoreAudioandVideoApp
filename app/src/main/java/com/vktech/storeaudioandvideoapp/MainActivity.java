package com.vktech.storeaudioandvideoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_AUDIO_REQUEST = 2;
    private static final int PICK_VIDEO_REQUEST = 3;

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    private Button buttonPickAudio, buttonPlayPauseAudio, buttonStopAudio;
    private Button buttonPickVideo, buttonPlayPauseVideo, buttonStopVideo;

    private String pendingPickType = ""; // "audio" or "video"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        buttonPickAudio = findViewById(R.id.buttonPickAudio);
        buttonPlayPauseAudio = findViewById(R.id.buttonPlayPauseAudio);
        buttonStopAudio = findViewById(R.id.buttonStopAudio);
        buttonPickVideo = findViewById(R.id.buttonPickVideo);
        buttonPlayPauseVideo = findViewById(R.id.buttonPlayPauseVideo);
        buttonStopVideo = findViewById(R.id.buttonStopVideo);
        videoView = findViewById(R.id.videoView);

        // Pick Audio
        buttonPickAudio.setOnClickListener(v -> {
            pendingPickType = "audio";
            checkPermissionAndPickFile();
        });

        // Play/Pause Audio
        buttonPlayPauseAudio.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    buttonPlayPauseAudio.setText("Play Audio");
                } else {
                    mediaPlayer.start();
                    buttonPlayPauseAudio.setText("Pause Audio");
                }
            } else {
                Toast.makeText(this, "Pick an audio file first", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop Audio
        buttonStopAudio.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                buttonPlayPauseAudio.setText("Play Audio"); // reset text
            }
        });

        // Pick Video
        buttonPickVideo.setOnClickListener(v -> {
            pendingPickType = "video";
            checkPermissionAndPickFile();
        });

        // Play/Pause Video
        buttonPlayPauseVideo.setOnClickListener(v -> {
            if (videoView != null && videoView.getDuration() > 0) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    buttonPlayPauseVideo.setText("Play Video");
                } else {
                    videoView.start();
                    buttonPlayPauseVideo.setText("Pause Video");
                }
            } else {
                Toast.makeText(this, "Pick a video file first", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop Video
        buttonStopVideo.setOnClickListener(v -> {
            if (videoView != null && videoView.isPlaying()) {
                videoView.stopPlayback();
                buttonPlayPauseVideo.setText("Play Video"); // reset text
            }
        });
    }

    private void checkPermissionAndPickFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean audioGranted = checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
            boolean videoGranted = checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;

            if ((pendingPickType.equals("audio") && audioGranted) ||
                    (pendingPickType.equals("video") && videoGranted)) {
                pickMediaFile();
            } else {
                if (pendingPickType.equals("audio")) {
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, PERMISSION_REQUEST_CODE);
                }
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickMediaFile();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void pickMediaFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (pendingPickType.equals("audio")) {
            intent.setType("audio/*");
        } else {
            intent.setType("video/*");
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, pendingPickType.equals("audio") ? PICK_AUDIO_REQUEST : PICK_VIDEO_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickMediaFile();
            } else {
                Toast.makeText(this, "Permission required to access media files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_AUDIO_REQUEST) {
                playAudioFromUri(uri);
            } else if (requestCode == PICK_VIDEO_REQUEST) {
                playVideoFromUri(uri);
            }
        }
    }

    private void playAudioFromUri(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            buttonPlayPauseAudio.setText("Pause Audio"); // set to pause after starting
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void playVideoFromUri(Uri uri) {
        videoView.setVideoURI(uri);
        videoView.start();
        buttonPlayPauseVideo.setText("Pause Video"); // set to pause after starting
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }
}
