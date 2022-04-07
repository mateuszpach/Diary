package com.github.mateuszpach.diary;

import androidx.lifecycle.ViewModel;

import com.github.mateuszpach.diary.data.Entry;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class EntryViewModel extends ViewModel {

    private final Repository repository;

    public EntryViewModel(Repository repository) {
        this.repository = repository;
    }

    public void addEntry(Entry entry) {
        repository.addEntry(entry).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteEntry(Entry entry) {
        repository.deleteEntry(entry).subscribeOn(Schedulers.io()).subscribe();
    }

    public Flowable<List<Entry>> getAllEntries() {
        return repository.getAllEntries();
    }

    public Flowable<Entry> getEntryById(int id) {
        return repository.getEntryById(id);
    }
}
