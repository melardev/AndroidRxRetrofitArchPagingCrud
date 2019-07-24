package com.melardev.android.crud.utils;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static String getFormatted(Date date) {
        return DateFormat.format("dd-MM-yyyy hh:mm:ss", date).toString();
    }
}
