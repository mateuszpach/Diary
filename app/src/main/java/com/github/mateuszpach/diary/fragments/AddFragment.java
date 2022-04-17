package com.github.mateuszpach.diary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.LocationGetter;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public abstract class AddFragment extends Fragment {

    private State state = State.IDLE;
    private EntryViewModel viewModel;

    private boolean locationDenied = false;
    private DisposableSingleObserver<String> locationObserver;
    private OnBackPressedCallback onBackPressedCallback;
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

        viewModel = Injection.provideEntryViewModel(this.getContext());

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                changeState(State.IDLE);
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
        safeDisposeLocationObserver();
    }

    private void safeDisposeLocationObserver() {
        if (locationObserver != null && !locationObserver.isDisposed()) {
            locationObserver.dispose();
            locationObserver = null;
        }
    }

    protected abstract boolean canCreateEntry();

    protected abstract Entry createEntry(Date currentDate, String location);

    protected abstract void navigateToCatalog();

    private void insertEntry() {
        if (!canCreateEntry()) {
            return;
        }

        LocationGetter locationGetter = new LocationGetter(getContext(),
                enableLocationLauncher,
                grantLocationPermissionsLauncher);

        Date currentDate = Calendar.getInstance().getTime();

        locationObserver = new DisposableSingleObserver<String>() {
            @Override
            public void onStart() {
                changeState(State.SAVING);
            }

            @Override
            public void onSuccess(String location) {
                Entry newEntry = createEntry(currentDate, location);
                viewModel.addEntry(newEntry);
                changeState(State.SAVED);
                navigateToCatalog();
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

    protected EntryViewModel getViewModel() {
        return viewModel;
    }

    protected enum State {
        IDLE, RECORDING, SAVING, SAVED
    }

    protected void changeState(State state) {
        if (this.state != state) {
            switch (state) {
                case IDLE:
                    onChangedToIdle();
                    break;
                case SAVING:
                    onChangedToSaving();
                    break;
                case RECORDING:
                    onChangedToRecording();
                    break;
                case SAVED:
                    onChangedToSaved();
                    break;
            }
            this.state = state;
        }
    }

    protected void onChangedToIdle() {
        safeDisposeLocationObserver();
        onBackPressedCallback.setEnabled(false);
        saveButton.setVisible(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);
    }

    protected void onChangedToSaving() {
        onBackPressedCallback.setEnabled(true);
        saveButton.setVisible(false);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setDisplayHomeAsUpEnabled(false);
    }

    protected void onChangedToRecording() {
    }

    protected void onChangedToSaved() {
    }

    protected State getState() {
        return state;
    }

    protected MenuItem getSaveButton() {
        return saveButton;
    }

}