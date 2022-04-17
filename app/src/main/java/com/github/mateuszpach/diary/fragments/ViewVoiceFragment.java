package com.github.mateuszpach.diary.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.DateFormatter;
import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.databinding.FragmentViewVoiceBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ViewVoiceFragment extends Fragment {

    private FragmentViewVoiceBinding binding;
    private EntryViewModel viewModel;
    private CompositeDisposable disposables;
    private MenuItem deleteButton;
    private Entry entry;
    private MediaPlayer mediaPlayer;
    private final Handler seekBarHandler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentViewVoiceBinding.inflate(inflater, container, false);
        viewModel = Injection.provideEntryViewModel(this.getContext());
        disposables = new CompositeDisposable();

        loadEntry();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.floatingActionButtonPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                binding.floatingActionButtonPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                mediaPlayer.pause();
            } else {
                binding.floatingActionButtonPlay.setImageResource(R.drawable.ic_baseline_pause_24);
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view, menu);
        deleteButton = menu.findItem(R.id.delete_button);
        deleteButton.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_button) {
            deleteEntry();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        seekBarHandler.removeCallbacksAndMessages(null);
        binding = null;
        disposables.dispose();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadEntry() {
        int id = ViewTextFragmentArgs.fromBundle(getArguments()).getEntryId();
        disposables.add(viewModel.getEntryById(id)
                .observeOn(Schedulers.io())
                .subscribe(entry -> requireActivity().runOnUiThread(() -> {
                    binding.dateTextView.setText(DateFormatter.format(entry.date));
                    binding.locationTextView.setText(entry.location);

                    this.entry = entry;
                    setupMediaPlayer();

                    deleteButton.setVisible(true);
                })));
    }

    private void setupMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(mp ->
                    binding.floatingActionButtonPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24));
            mediaPlayer.setDataSource(entry.content);
            mediaPlayer.prepare();
            binding.lengthTextView.setText(new SimpleDateFormat("mm:ss", Locale.UK)
                    .format(new Date(mediaPlayer.getDuration())));
            binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo((mediaPlayer.getDuration() / 100) * progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            seekBarProgressUpdater();
        } catch (IOException e) {
            Toast toast = Toast.makeText(getContext(), "Failed to load the recording", Toast.LENGTH_SHORT);
            toast.show();
            binding.floatingActionButtonPlay.setVisibility(View.INVISIBLE);
            binding.seekBar.setVisibility(View.INVISIBLE);
            binding.timerTextView.setVisibility(View.INVISIBLE);
            binding.lengthTextView.setVisibility(View.INVISIBLE);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void seekBarProgressUpdater() {
        binding.timerTextView.setText(new SimpleDateFormat("mm:ss", Locale.UK)
                .format(new Date(mediaPlayer.getCurrentPosition())));

        ObjectAnimator animation = ObjectAnimator.ofInt(binding.seekBar,
                "progress",
                (int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
        animation.setDuration(100);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        seekBarHandler.postDelayed(this::seekBarProgressUpdater, 50);
    }

    private void deleteEntry() {
        File file = new File(entry.content);
        if (file.exists()) {
            boolean ignore = file.delete();
        }
        viewModel.deleteEntry(entry);

        NavHostFragment.findNavController(ViewVoiceFragment.this)
                .navigate(R.id.action_ViewVoiceFragment_to_CatalogFragment);
    }

}