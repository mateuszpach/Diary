package com.github.mateuszpach.diary.data;

import java.util.List;

import io.reactivex.Flowable;

public class RoomRepository {

    private EntryDao entryDao;

    public RoomRepository(EntryDao entryDao) {
        this.entryDao = entryDao;
    }

    Flowable<List<Entry>> readAllEntries = entryDao.readAllEntries();


}
