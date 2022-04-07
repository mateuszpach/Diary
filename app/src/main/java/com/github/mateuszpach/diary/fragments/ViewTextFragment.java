package com.github.mateuszpach.diary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.github.mateuszpach.diary.DateFormatter;
import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.databinding.FragmentViewTextBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ViewTextFragment extends Fragment {

    private FragmentViewTextBinding binding;
    private EntryViewModel viewModel;
    private CompositeDisposable disposables;
    private MenuItem deleteButton;
    private Entry entry;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentViewTextBinding.inflate(inflater, container, false);
        viewModel = Injection.provideAddTextViewModel(this.getContext());
        disposables = new CompositeDisposable();

        loadEntry();

        return binding.getRoot();
    }

    private void loadEntry() {
        int id = ViewTextFragmentArgs.fromBundle(getArguments()).getEntryId();
        disposables.add(viewModel.getEntryById(id)
                .observeOn(Schedulers.io())
                .subscribe(entry -> getActivity().runOnUiThread(() -> {
                    binding.dateTextView.setText(DateFormatter.format(entry.date));
                    binding.locationTextView.setText(entry.location);
                    binding.contentTextView.setText(entry.content);
                    this.entry = entry;
                    deleteButton.setVisible(true);
                })));
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
        binding = null;
        disposables.dispose();
    }

    private void deleteEntry() {
        viewModel.deleteEntry(entry);

        NavHostFragment.findNavController(ViewTextFragment.this)
                .navigate(R.id.action_ViewTextFragment_to_CatalogFragment);
    }

}