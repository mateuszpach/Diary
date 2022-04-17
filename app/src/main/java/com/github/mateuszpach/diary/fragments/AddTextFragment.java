package com.github.mateuszpach.diary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddTextBinding;

import java.util.Date;

public class AddTextFragment extends AddFragment {

    private FragmentAddTextBinding binding;
    private String content;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentAddTextBinding.inflate(inflater, container, false);

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected boolean canCreateEntry() {
        content = binding.editText.getText().toString();
        if (content.isEmpty()) {
            Toast toast = Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return true;
    }

    @Override
    protected Entry createEntry(Date currentDate, String location) {
        return new Entry(0, currentDate, location, EntryType.TEXT, content);
    }

    @Override
    protected void navigateToCatalog() {
        NavHostFragment.findNavController(AddTextFragment.this)
                .navigate(R.id.action_AddTextFragment_to_CatalogFragment);
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