package github.tornaco.xposedmoduletest.xposed.util;

import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;

/**
 * Created by Tornaco on 2018/5/1 19:21.
 * God bless no bug!
 */
public class ClazzDumper {

    public static final Printer XPOSED_LOG_PRINTER = new Printer() {
        @Override
        public void start() {

        }

        @Override
        public void println(String line) {
            XposedLog.wtf(line);
        }

        @Override
        public void end() {

        }
    };

    public static final Printer ANDROID_UTIL_LOG_PRINTER = new Printer() {
        @Override
        public void start() {

        }

        @Override
        public void println(String line) {
            Log.d(XposedLog.TAG, line);
        }

        @Override
        public void end() {

        }
    };

    public static class FilePrinter implements Printer {

        private String fileName;

        private StringBuilder sb;

        public FilePrinter(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void start() {
            this.sb = new StringBuilder();
        }

        @Override
        public void println(String line) {
            sb.append(line).append("\n");
            ANDROID_UTIL_LOG_PRINTER.println(line);
        }

        @Override
        public void end() {
            File traceDir = RepoProxy.getDebugDumpDirByVersion();
            String fileNameFixed = fileName + "-" + DateUtils.formatForFileName(System.currentTimeMillis());
            File f = new File(traceDir, fileNameFixed);
            try {
                Files.createParentDirs(f);
                Files.asByteSink(f).asCharSink(Charset.defaultCharset())
                        .write(sb.toString());
            } catch (Throwable ignored) {
            }
        }
    }

    public interface Printer {
        void start();

        void println(String line);

        void end();
    }

    public static void dump(Class clazz, Printer printer) {
        printer.start();
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
        printer.end();
    }
}
