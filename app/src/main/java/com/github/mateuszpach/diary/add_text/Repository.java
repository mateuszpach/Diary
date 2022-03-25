package com.github.mateuszpach.diary.add_text;

import com.github.mateuszpach.diary.data.Entry;

import java.util.List;

import io.reactivex.Flowable;

public interface Repository {
    Flowable<List<Entry>> readAllEntries();
}
