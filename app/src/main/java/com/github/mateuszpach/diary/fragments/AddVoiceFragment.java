package com.github.mateuszpach.diary.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddVoiceBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddVoiceFragment extends AddFragment {

    private FragmentAddVoiceBinding binding;
    private final long MAX_RECORDING_TIME = 60 * 60 * 1000 + 1000;
    private String storagePath;
    private final String recordingFilename = UUID.randomUUID().toString();
    private MediaRecorder recorder;

    private final CountDownTimer timer = new CountDownTimer(MAX_RECORDING_TIME, 1000) {
        @Override
        public void onTick(long left) {
            binding.timerTextView.setText(new SimpleDateFormat("mm:ss", Locale.UK)
                    .format(new Date(MAX_RECORDING_TIME - left)));
            if (left < 2000) {

                stopRecording();
            }
        }

        @Override
        public void onFinish() {
        }
    };

    private final ActivityResultLauncher<String> grantAudioRecordingPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (!result) {
                    Toast toast = Toast.makeText(getContext(), "Audio recording permission denied.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    startRecording();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentAddVoiceBinding.inflate(inflater, container, false);
        storagePath = requireContext().getFilesDir().getAbsolutePath();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.floatingActionButtonRecord.setOnClickListener(v -> {
            if (getState() == State.RECORDING) {
                stopRecording();
            } else {
                startRecording();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getSaveButton().setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel();
        if (getState() != State.SAVED) {
            File file = new File(storagePath, recordingFilename);
            if (file.exists()) {
                boolean ignore = file.delete();
            }
        }
        binding = null;
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            grantAudioRecordingPermissionsLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(storagePath + "/" + recordingFilename);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                recorder.prepare();
            } catch (IOException e) {
                Toast toast = Toast.makeText(getContext(), "Failed to open recorder", Toast.LENGTH_SHORT);
                toast.show();
            }

            recorder.start();
            timer.start();
            changeState(State.RECORDING);
        }
    }

    private void stopRecording() {
        changeState(State.IDLE);
        timer.cancel();
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    protected boolean canCreateEntry() {
        File file = new File(storagePath, recordingFilename);
        if (!file.exists()) {
            Toast toast = Toast.makeText(getContext(), "Recording cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    @Override
    protected Entry createEntry(Date currentDate, String location) {
        System.out.println(storagePath + "/" + recordingFilename);
        return new Entry(0, currentDate, location, EntryType.VOICE, storagePath + "/" + recordingFilename);
    }

    @Override
    protected void navigateToCatalog() {
        NavHostFragment.findNavController(AddVoiceFragment.this)
                .navigate(R.id.action_AddVoiceFragment_to_CatalogFragment);
    }

    @Override
    protected void onChangedToIdle() {
        super.onChangedToIdle();
        binding.progressBar.setVisibility(View.GONE);
        binding.floatingActionButtonRecord.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
    }

    @Override
    protected void onChangedToSaving() {
        super.onChangedToSaving();
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onChangedToRecording() {
        super.onChangedToRecording();
        binding.floatingActionButtonRecord.setImageResource(R.drawable.ic_baseline_stop_24);
        getSaveButton().setVisible(false);
    }
}