package com.github.mateuszpach.diary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddDrawingBinding;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class AddDrawingFragment extends AddFragment {

    private FragmentAddDrawingBinding binding;
    private String storagePath;
    private final String recordingFilename = UUID.randomUUID().toString();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentAddDrawingBinding.inflate(inflater, container, false);
        storagePath = requireContext().getFilesDir().getAbsolutePath();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getState() != State.SAVED) {
            File file = new File(storagePath, recordingFilename);
            if (file.exists()) {
                boolean ignore = file.delete();
            }
        }
        binding = null;
    }

    @Override
    protected boolean canCreateEntry() {
        File file = new File(storagePath, recordingFilename);
        binding.drawingCanvas.saveToFile(file);
        if (!file.exists()) {
            Toast toast = Toast.makeText(getContext(), "Drawing cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    @Override
    protected Entry createEntry(Date currentDate, String location) {
        System.out.println(storagePath + "/" + recordingFilename);
        return new Entry(0, currentDate, location, EntryType.DRAWING, storagePath + "/" + recordingFilename);
    }

    @Override
    protected void navigateToCatalog() {
        NavHostFragment.findNavController(AddDrawingFragment.this)
                .navigate(R.id.action_AddDrawingFragment_to_CatalogFragment);
    }

    @Override
    protected void onChangedToIdle() {
        super.onChangedToIdle();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onChangedToSaving() {
        super.onChangedToSaving();
        binding.progressBar.setVisibility(View.VISIBLE);
    }
}