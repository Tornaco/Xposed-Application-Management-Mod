package github.tornaco.xposedmoduletest.xposed.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Nick@NewStand.org on 2017/3/26 17:00
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class DateUtils {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd-HH:mm:ss";

    public static String formatLong(long l) {
        String time;
        DateFormat format = DateFormat.getDateInstance(DateFormat.FULL);
        Date d1 = new Date(l);
        time = format.format(d1);
        DateFormat timeInstance = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return time + "\t" + timeInstance.format(d1);
    }

    public static String formatForFileName(long l) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        Date d1 = new Date(l);
        return format.format(d1);
    }
}