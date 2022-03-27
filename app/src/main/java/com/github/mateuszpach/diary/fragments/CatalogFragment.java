package com.github.mateuszpach.diary.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mateuszpach.diary.EntryViewModel;
import com.github.mateuszpach.diary.Injection;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.databinding.FragmentCatalogBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;
    private EntryViewModel viewModel;
    private CompositeDisposable disposables;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        viewModel = Injection.provideAddTextViewModel(this.getContext());
        disposables = new CompositeDisposable();

        setupRecyclerView();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        ListAdapter adapter = new ListAdapter();
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        disposables.add(viewModel.getAllEntries()
                .observeOn(Schedulers.io())
                .subscribe(entries -> {
                    adapter.setData(entries);
                    getActivity().runOnUiThread(adapter::notifyDataSetChanged);
                }));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.floatingActionButton.setOnClickListener(v -> NavHostFragment
                .findNavController(CatalogFragment.this)
                .navigate(R.id.action_CatalogFragment_to_AddTextFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.dispose();
    }

}