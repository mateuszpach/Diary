package com.github.mateuszpach.diary;

import com.github.mateuszpach.diary.data.Entry;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface Repository {
    Completable addEntry(Entry entry);

    Flowable<List<Entry>> getAllEntries();

    Flowable<Entry> getEntryById(int id);
}
