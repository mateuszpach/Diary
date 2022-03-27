package com.github.mateuszpach.diary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatDate {
    public static String format(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ROOT);
        return dateFormat.format(date).toUpperCase(Locale.ROOT);
    }
}
