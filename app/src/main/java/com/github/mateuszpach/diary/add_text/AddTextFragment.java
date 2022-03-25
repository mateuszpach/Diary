package com.github.mateuszpach.diary.add_text;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddTextBinding;

import java.util.Calendar;
import java.util.Date;

public class AddTextFragment extends Fragment {

    private FragmentAddTextBinding binding;
    private EditText editText;
    private AddTextViewModel addTextViewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddTextBinding.inflate(inflater, container, false);
        editText = getView().findViewById(R.id.editText);

//        userViewModel = ViewModelProvider(this).get();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertEntry();
                NavHostFragment.findNavController(AddTextFragment.this)
                        .navigate(R.id.action_AddTextFragment_to_CatalogFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void insertEntry() {
        String content = editText.getText().toString();
        Date currentDate = Calendar.getInstance().getTime();
        String location = "Krakow";

        if (!content.isEmpty()) {
            Entry newEntry = new Entry(0, currentDate, location, EntryType.TEXT);
        }


    }

}