package com.github.mateuszpach.diary.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addEntry(Entry entry);

    @Delete
    void deleteEntry(Entry entry);

    @Query("SELECT * FROM entry ORDER BY date DESC")
    Flowable<List<Entry>> getAllEntries();

    @Query("SELECT * FROM entry WHERE id=:id")
    Flowable<Entry> getEntryById(int id);
}
