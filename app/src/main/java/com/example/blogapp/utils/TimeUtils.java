package com.example.blogapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(String serverTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault());
        try {
            Date serverDate = sdf.parse(serverTime);
            if (serverDate != null) {
                long timeDifference = System.currentTimeMillis() - serverDate.getTime();
                return formatTimeDifference(timeDifference);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Không xác định";
    }

    private static String formatTimeDifference(long timeDifference) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifference);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifference);

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else if (seconds > 0) {
            return seconds + " giây trước";
        } else {
            return "Vừa xong";
        }
    }
}
