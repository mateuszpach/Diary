package com.github.mateuszpach.diary.data;

import com.github.mateuszpach.diary.Repository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class RoomRepository implements Repository {

    private final EntryDao entryDao;

    public RoomRepository(EntryDao entryDao) {
        this.entryDao = entryDao;
    }

    @Override
    public Completable addEntry(Entry entry) {
        return Completable.fromAction(() -> entryDao.addEntry(entry));
    }

    @Override
    public Flowable<List<Entry>> getAllEntries() {
        return entryDao.getAllEntries();
    }

    @Override
    public Flowable<Entry> getEntryById(int id) {
        return entryDao.getEntryById(id);
    }
}
