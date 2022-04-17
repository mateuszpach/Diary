package com.github.mateuszpach.diary;

import android.content.Context;

import com.github.mateuszpach.diary.data.AppDatabase;
import com.github.mateuszpach.diary.data.RoomRepository;

public class Injection {
    public static RoomRepository provideRoomRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        return new RoomRepository(database.entryDao());
    }

    public static EntryViewModel provideEntryViewModel(Context context) {
        return new EntryViewModel(provideRoomRepository(context));
    }
}
