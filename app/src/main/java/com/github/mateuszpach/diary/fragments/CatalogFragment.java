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
    private boolean isExpandedMenu;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        viewModel = Injection.provideEntryViewModel(this.getContext());
        disposables = new CompositeDisposable();

        setupRecyclerView();
        isExpandedMenu = false;

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
                    requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                }));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.floatingActionButtonMenu.setOnClickListener(v -> {
            if (!isExpandedMenu) {
                binding.floatingActionButtonText.show();
                binding.floatingActionButtonVoice.show();
                binding.floatingActionButtonVideo.show();
                binding.floatingActionButtonDrawing.show();
                binding.floatingActionButtonMenu.setImageResource(R.drawable.ic_baseline_close_24);
                isExpandedMenu = true;
            } else {
                binding.floatingActionButtonText.hide();
                binding.floatingActionButtonVoice.hide();
                binding.floatingActionButtonVideo.hide();
                binding.floatingActionButtonDrawing.hide();
                binding.floatingActionButtonMenu.setImageResource(R.drawable.ic_baseline_add_24);
                isExpandedMenu = false;
            }
        });

        binding.floatingActionButtonText.setOnClickListener(v -> NavHostFragment
                .findNavController(CatalogFragment.this)
                .navigate(R.id.action_CatalogFragment_to_AddTextFragment));

        binding.floatingActionButtonVoice.setOnClickListener(v -> NavHostFragment
                .findNavController(CatalogFragment.this)
                .navigate(R.id.action_CatalogFragment_to_AddVoiceFragment));

        binding.floatingActionButtonVideo.setOnClickListener(v -> NavHostFragment
                .findNavController(CatalogFragment.this)
                .navigate(R.id.action_CatalogFragment_to_AddVideoFragment));

        binding.floatingActionButtonDrawing.setOnClickListener(v -> NavHostFragment
                .findNavController(CatalogFragment.this)
                .navigate(R.id.action_CatalogFragment_to_AddDrawingFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.dispose();
    }

}