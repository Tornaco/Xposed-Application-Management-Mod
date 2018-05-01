package github.tornaco.xposedmoduletest.xposed.util;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Tornaco on 2018/5/1 19:21.
 * God bless no bug!
 */
public class ClazzDumper {

    public static final Printer XPOSED_LOG_PRINTER = XposedLog::wtf;

    public static final Printer ANDROID_UTIL_LOG_PRINTER = line -> Log.d(XposedLog.TAG, line);

    public interface Printer {
        void println(String line);
    }

    public static void dump(Class clazz, Printer printer) {
        // Dump class header.
        printer.println(String.format("\n**** CLAZZ DUMPER START DUMP OF %s ***", clazz));
        // Dump methods.
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                printer.println(String.format("METHOD OF CLASS %s: %s ", searchType, method));
            }

            // Dump class fields.
            Field[] fields = searchType.isInterface() ? null : clazz.getDeclaredFields();
            if (fields != null) for (Field field : fields) {
                printer.println(String.format("FIELD OF CLASS %s: %s", searchType, field));
            }

            searchType = searchType.getSuperclass();
        }
        // End.
        printer.println(String.format("**** CLAZZ DUMPER END DUMP OF %s ***\n", clazz));
    }
}
