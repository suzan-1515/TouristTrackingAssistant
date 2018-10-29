package com.nepal.tia.utils;

import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    public static String format(long timeInMillis) {
        return format(convert(timeInMillis));
    }

    public static String format(Calendar calendar) {
        return String.format(Locale.getDefault()
                , "%1$tb %1$te, %1$tY"
                , calendar);
    }

    public static Calendar convert(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }
}
