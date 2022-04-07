package com.github.mateuszpach.diary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.LocationGetter;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryType;
import com.github.mateuszpach.diary.databinding.FragmentAddTextBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AddTextFragment extends Fragment {

    private FragmentAddTextBinding binding;
    private EntryViewModel viewModel;
    private DisposableSingleObserver<String> locationObserver;
    private OnBackPressedCallback onBackPressedCallback;
    private boolean locationDenied = false;
    private MenuItem saveButton;


    private final ActivityResultLauncher<IntentSenderRequest> enableLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    locationDenied = true;
                }
                insertEntry();
            });

    private final ActivityResultLauncher<String> grantLocationPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (!result) {
                    locationDenied = true;
                }
                insertEntry();
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddTextBinding.inflate(inflater, container, false);
        viewModel = Injection.provideAddTextViewModel(this.getContext());

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                safeDisposeLocationObserver();
                onBackPressedCallback.setEnabled(false);
                binding.progressBar.setVisibility(View.GONE);
                saveButton.setVisible(true);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                        .setDisplayHomeAsUpEnabled(true);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);
        saveButton = menu.findItem(R.id.save_button);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_button) {
            insertEntry();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        safeDisposeLocationObserver();
    }

    private void safeDisposeLocationObserver() {
        if (locationObserver != null && !locationObserver.isDisposed()) {
            locationObserver.dispose();
            locationObserver = null;
        }
    }

    private void insertEntry() {
        LocationGetter locationGetter = new LocationGetter(getContext(),
                enableLocationLauncher,
                grantLocationPermissionsLauncher);

        String content = binding.editText.getText().toString();

        if (content.isEmpty()) {
            Toast toast = Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Date currentDate = Calendar.getInstance().getTime();

        locationObserver = new DisposableSingleObserver<String>() {
            @Override
            public void onStart() {
                binding.progressBar.setVisibility(View.VISIBLE);
                onBackPressedCallback.setEnabled(true);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                        .setDisplayHomeAsUpEnabled(false);
                saveButton.setVisible(false);
            }

            @Override
            public void onSuccess(String location) {
                Entry newEntry = new Entry(0, currentDate, location, EntryType.TEXT, content);
                viewModel.addEntry(newEntry);

                NavHostFragment.findNavController(AddTextFragment.this)
                        .navigate(R.id.action_AddTextFragment_to_CatalogFragment);
            }

            @Override
            public void onError(Throwable e) {
            }
        };

        if (locationDenied) {
            locationObserver.onSuccess("");
        } else {
            Single.fromCallable(locationGetter::getLocation)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(locationObserver);
        }

    }

}