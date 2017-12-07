package github.tornaco.xposedmoduletest.xposed.service;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.util.Arrays;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@ToString
public class NativeDaemonConnector {

    @NonNull
    private Object connectObject;

    public void execute(String command, Object... args)
            throws NativeDaemonConnectorException {
        XposedLog.verbose("NativeDaemonConnector, execute: " + command + Arrays.toString(args));
        try {
            XposedHelpers.callMethod(connectObject, "execute", command, args);
        } catch (Throwable e) {
            throw new NativeDaemonConnectorException(e);
        }
    }

    public class NativeDaemonConnectorException extends Exception {
        public NativeDaemonConnectorException() {
        }

        public NativeDaemonConnectorException(String message) {
            super(message);
        }

        public NativeDaemonConnectorException(String message, Throwable cause) {
            super(message, cause);
        }

        public NativeDaemonConnectorException(Throwable cause) {
            super(cause);
        }

        /**
         * Rethrow as a {@link RuntimeException} subclass that is handled by
         * {@link Parcel#writeException(Exception)}.
         */
        public IllegalArgumentException rethrowAsParcelableException() {
            throw new IllegalStateException(getMessage(), this);
        }
    }

}
