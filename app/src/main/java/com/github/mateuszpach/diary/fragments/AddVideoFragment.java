package com.github.mateuszpach.diary.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddVideoBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AddVideoFragment extends AddFragment {

    private FragmentAddVideoBinding binding;
    private final long MAX_RECORDING_TIME = 60 * 60 * 1000 + 1000;
    private String storagePath;
    private final String recordingFilename = UUID.randomUUID().toString();
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

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

    private final ActivityResultLauncher<String[]> grantAudioCameraRecordingPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {
                for (Boolean result : results.values()) {
                    if (!result) {
                        Toast toast = Toast.makeText(getContext(), "Audio recording or Camera permission denied.", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                }
                startCamera();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentAddVideoBinding.inflate(inflater, container, false);
        storagePath = requireContext().getFilesDir().getAbsolutePath();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            grantAudioCameraRecordingPermissionsLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
        } else {
            startCamera();
        }

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

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                PreviewView viewFinder = binding.preview;

                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.bindToLifecycle(AddVideoFragment.this, cameraSelector, preview, videoCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(AddVideoFragment.this.getContext(), "Failed to open camera.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("MissingPermission") // already checked
    private void startRecording() {
        File file = new File(storagePath, recordingFilename);
        FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(file).build();

        recording = videoCapture.getOutput()
                .prepareRecording(requireContext(), fileOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext()), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        changeState(State.RECORDING);
                        timer.start();
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                        if (!finalizeEvent.hasError()) {
                            String msg = "Video capture succeeded: " +
                                    finalizeEvent.getOutputResults().getOutputUri();
                            System.out.println(msg);
                            timer.cancel();
                            changeState(State.IDLE);
                        } else {
                            if (recording != null) {
                                recording.close();
                            }
                            recording = null;
                        }
                    }
                });
    }

    private void stopRecording() {
        changeState(State.IDLE);
        timer.cancel();
        if (recording != null) {
            recording.stop();
            recording = null;
        }
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
        return new Entry(0, currentDate, location, EntryType.VIDEO, storagePath + "/" + recordingFilename);
    }

    @Override
    protected void navigateToCatalog() {
        NavHostFragment.findNavController(AddVideoFragment.this)
                .navigate(R.id.action_AddVideoFragment_to_CatalogFragment);
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