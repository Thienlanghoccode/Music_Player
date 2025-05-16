package com.example.musicapp.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.musicapp.R;
import com.example.musicapp.databinding.FragmentMusicPlayerBinding;
import com.example.musicapp.entity.Song;
import com.example.musicapp.service.MusicService;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicPlayerFragment extends Fragment {

    private static final String TAG = "MusicPlayerFragment";
    private FragmentMusicPlayerBinding binding;
    private MusicService musicService;
    private boolean isServiceBound = false;
    private Song currentSong;
    private NavController navController;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarTask;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            updateSongInfo();
            updatePlayPauseButton();
            initializeSeekBar();
            startSeekBarUpdate();
            Log.d(TAG, "Đã kết nối với MusicService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            musicService = null;
            Log.d(TAG, "Ngắt kết nối với MusicService");
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        initializeSong();
        setupListeners();
        bindMusicService();
    }

    private void initializeSong() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("current_song")) {
            currentSong = args.getParcelable("current_song");
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        binding.playPauseButton.setOnClickListener(v -> togglePlayPause());

        binding.nextButton.setOnClickListener(v -> playNext());

        binding.prevButton.setOnClickListener(v -> playPrevious());

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isServiceBound && musicService != null) {
                    musicService.seekTo(progress);
                    updateTimeText(progress, seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isServiceBound && musicService != null && musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }
        });
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(requireActivity(), MusicService.class);
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void togglePlayPause() {
        if (!isServiceBound || musicService == null) return;

        if (musicService.isPlaying()) {
            musicService.pauseSong();
            binding.playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateSeekBarTask);
        } else {
            musicService.playSong(currentSong != null ? currentSong.getPreviewUrl() : null, currentSong);
            binding.playPauseButton.setImageResource(R.drawable.ic_pause);
            startSeekBarUpdate();
        }
    }

    private void playNext() {
        if (!isServiceBound || musicService == null) return;

        musicService.playNext();
        currentSong = musicService.getCurrentSong();
        updateSongInfo();
        updatePlayPauseButton();
        initializeSeekBar();
        startSeekBarUpdate();
    }

    private void playPrevious() {
        if (!isServiceBound || musicService == null) return;

        musicService.playPrevious();
        currentSong = musicService.getCurrentSong();
        updateSongInfo();
        updatePlayPauseButton();
        initializeSeekBar();
        startSeekBarUpdate();
    }

    private void updateSongInfo() {
        if (currentSong != null) {
            binding.songTitleText.setText(currentSong.getTitle());
            binding.songArtistText.setText(currentSong.getArtist());
            binding.noMusicText.setVisibility(View.GONE);

            if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(currentSong.getImageUrl())
                        .into(binding.albumArt);
                binding.albumArt.setVisibility(View.VISIBLE);
                binding.albumArtPlaceholder.setVisibility(View.GONE);
            } else {
                binding.albumArt.setVisibility(View.GONE);
                binding.albumArtPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            binding.songTitleText.setText(R.string.no_music_selected);
            binding.songArtistText.setText("");
            binding.noMusicText.setVisibility(View.VISIBLE);
            binding.albumArt.setVisibility(View.GONE);
            binding.albumArtPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void updatePlayPauseButton() {
        if (!isServiceBound || musicService == null) return;

        binding.playPauseButton.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
    }


    private void initializeSeekBar() {
        if (!isServiceBound || musicService == null) return;

        long duration = musicService.getDuration();
        if (duration > 0) {
            binding.seekBar.setMax((int) duration);
            updateTimeText(0, duration);
        } else {
            binding.seekBar.setMax(0);
            binding.totalDurationText.setText(R.string.no_time);
            binding.currentTimeText.setText(R.string.no_time);
        }
    }


    private void startSeekBarUpdate() {
        if (updateSeekBarTask == null) {
            updateSeekBarTask = new Runnable() {
                @Override
                public void run() {
                    if (isServiceBound && musicService != null && musicService.isPlaying()) {
                        long currentPosition = musicService.getCurrentPosition();
                        binding.seekBar.setProgress((int) currentPosition);
                        updateTimeText(currentPosition, binding.seekBar.getMax());
                        handler.postDelayed(this, 1000);
                    }
                }
            };
        }
        handler.removeCallbacks(updateSeekBarTask);
        handler.post(updateSeekBarTask);
    }


    private void updateTimeText(long currentPosition, long duration) {
        binding.currentTimeText.setText(formatTime(currentPosition));
        binding.totalDurationText.setText(formatTime(duration));
    }


    private String formatTime(long timeMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateSeekBarTask);
        if (isServiceBound) {
            requireActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
        binding = null;
    }
}