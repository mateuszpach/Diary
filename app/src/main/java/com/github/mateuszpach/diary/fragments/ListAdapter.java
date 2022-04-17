package com.github.mateuszpach.diary.fragments;

import static androidx.navigation.Navigation.findNavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mateuszpach.diary.DateFormatter;
import com.github.mateuszpach.diary.R;
import com.github.mateuszpach.diary.data.Entry;

import java.util.LinkedList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Entry> entries = new LinkedList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.fragment_catalog_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entries.get(position);
        ((TextView) holder.itemView.findViewById(R.id.locationTextView)).setText(entry.location);
        ((TextView) holder.itemView.findViewById(R.id.dateTextView)).setText(DateFormatter.format(entry.date));
        ImageView icon = holder.itemView.findViewById(R.id.icon);
        NavDirections action = null;
        switch (entry.entryType) {
            case TEXT:
                icon.setImageResource(R.drawable.ic_baseline_create_24);
                action = CatalogFragmentDirections.actionCatalogFragmentToViewTextFragment(entry.id);
                break;
            case VOICE:
                icon.setImageResource(R.drawable.ic_baseline_mic_24);
                action = CatalogFragmentDirections.actionCatalogFragmentToViewVoiceFragment(entry.id);
                break;
            case VIDEO:
                icon.setImageResource(R.drawable.ic_baseline_videocam_24);
//                action = CatalogFragmentDirections.actionCatalogFragmentToViewVideoFragment(entry.id);
                break;
            case DRAWING:
                icon.setImageResource(R.drawable.ic_baseline_insert_photo_24);
//                action = CatalogFragmentDirections.actionCatalogFragmentToViewDrawFragment(entry.id);
                break;
        }

        if (action != null) {
            final NavDirections chosenAction = action;
            holder.itemView.findViewById(R.id.catalogRow).setOnClickListener(v ->
                    findNavController(holder.itemView).navigate(chosenAction));
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void setData(List<Entry> entries) {
        this.entries = entries;
    }
}
