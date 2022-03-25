package com.github.mateuszpach.diary.add_text;

import com.github.mateuszpach.diary.data.Entry;
import com.github.mateuszpach.diary.data.EntryDao;

import java.util.List;

import io.reactivex.Flowable;

public class AddTextViewModel {

    Repository repository;
    public AddTextViewModel(Repository repository) {
        this.repository = repository;
    }

    Flowable<List<Entry>> readAllEntries = repository.readAllEntries();
}
