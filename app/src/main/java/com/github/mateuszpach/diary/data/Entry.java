package com.github.mateuszpach.diary.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Entry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public Date date;
    public String location;
    public EntryType entryType;
    public String content;

    public Entry(int id, Date date, String location, EntryType entryType, String content) {
        this.id = id;
        this.date = date;
        this.location = location;
        this.entryType = entryType;
        this.content = content;
    }
}
