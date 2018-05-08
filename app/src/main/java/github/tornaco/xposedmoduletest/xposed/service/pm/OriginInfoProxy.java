package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.util.Log;

import com.google.common.io.Files;

import java.io.File;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/8 13:25.
 * God bless no bug!
 */
public class OriginInfoProxy extends InvokeTargetProxy<Object> {

    public OriginInfoProxy(Object host) {
        super(host);
    }

    public File getFile() {
        try {
            File file = (File) XposedHelpers.getObjectField(getHost(), "file");
            XposedLog.verbose(XposedLog.PREFIX_PM + "OriginInfoProxy file: " + file);
            if (file.isFile()) {
                return file;
            }
            // Check if it is .apk
            String ext = Files.getFileExtension(file.getAbsolutePath());
            XposedLog.verbose(XposedLog.PREFIX_PM + "OriginInfoProxy ext: " + ext);
            if (ext != null && ext.contains(".apk")) {
                return file;
            }
            // Find .apk file.
            String candidateName = file.getAbsolutePath() + File.separator + "base.apk";
            File candidateFile = new File(candidateName);
            boolean exist = candidateFile.exists();
            XposedLog.verbose(XposedLog.PREFIX_PM + "OriginInfoProxy candidateFile: " + candidateFile + ", exist? " + exist);
            if (exist) {
                return candidateFile;
            } else {
                return file;
            }
        } catch (Throwable e) {
            XposedLog.wtf("OriginInfoProxy fail getFile: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
