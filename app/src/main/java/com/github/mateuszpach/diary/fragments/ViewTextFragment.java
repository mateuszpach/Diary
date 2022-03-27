package com.github.mateuszpach.diary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.FormatDate;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.databinding.FragmentViewTextBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ViewTextFragment extends Fragment {

    private FragmentViewTextBinding binding;
    private EntryViewModel viewModel;
    private CompositeDisposable disposables;

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
                .subscribe(entry -> getActivity().runOnUiThread(()->{
                    binding.dateTextView.setText(FormatDate.format(entry.date));
                    binding.locationTextView.setText(entry.location);
                    binding.contentTextView.setText(entry.content);
                })));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.dispose();
    }

}