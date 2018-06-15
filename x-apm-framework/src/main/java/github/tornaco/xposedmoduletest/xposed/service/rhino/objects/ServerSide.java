package github.tornaco.xposedmoduletest.xposed.service.rhino.objects;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.os.BackgroundThread;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * This class provides for sharing functions across multiple threads.
 */
@SuppressWarnings("WeakerAccess")
public class ServerSide extends ImporterTopLevel {

    static final long serialVersionUID = 4029130780977538005L;

    private boolean initialized;

    private android.content.Context androidContext;

    // FIXME This is not UI.
    private Handler uiHandler = BackgroundThread.getHandler();

    public ServerSide(Context cx, android.content.Context ax) {
        init(cx);
        this.androidContext = ax;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void init(ContextFactory factory) {
        factory.call(cx -> {
            init(cx);
            return null;
        });
    }

    private void init(Context cx) {
        initStandardObjects(cx, false);
        String[] names = FunctionProperties.ClazzParser
                .getAllFunctionProperties(ServerSide.class);
        XposedLog.debug("defineFunctionProperties:\n" + Arrays.toString(names));
        defineFunctionProperties(names, ServerSide.class,
                ScriptableObject.DONTENUM);

        initialized = true;
    }

    @FunctionProperties
    public void log(String log) {
        XposedLog.debug(log);
    }

    @FunctionProperties
    public void toast(String text) {
        uiHandler.post(new ErrorCatchRunnable(() -> Toast.makeText(androidContext, text, Toast.LENGTH_SHORT).show(), "toast"));
    }

    // INPUT START.
    @FunctionProperties
    public void swipe(int x1, int y1, int x2, int y2) {
        XAPMManager.get().executeInputCommand(new String[]{
                "swipe",
                String.valueOf(x1),
                String.valueOf(y1),
                String.valueOf(x2),
                String.valueOf(y2)
        });
    }

    @FunctionProperties
    public void tap(int x, int y) {
        XAPMManager.get().executeInputCommand(new String[]{
                "tap",
                String.valueOf(x),
                String.valueOf(y)
        });
    }

    @FunctionProperties
    public void input(String text) {
        XAPMManager.get().executeInputCommand(new String[]{
                "text",
                text
        });
    }

    @FunctionProperties
    public void power() {
        XAPMManager.get().injectPowerEvent();
    }

    @FunctionProperties
    public void home() {
        keyevent(KeyEvent.KEYCODE_HOME);
    }

    @FunctionProperties
    public void back() {
        keyevent(KeyEvent.KEYCODE_BACK);
    }

    @FunctionProperties
    public void menu() {
        keyevent(KeyEvent.KEYCODE_MENU);
    }

    @FunctionProperties
    public void keyevent(int key) {
        XAPMManager.get().executeInputCommand(new String[]{"keyevent", String.valueOf(key)});
    }

    @FunctionProperties
    public void threadWait(int timeMills) {
        try {
            Thread.sleep(timeMills);
        } catch (InterruptedException ignored) {

        }
    }

    @FunctionProperties
    public String getTopPackage() {
        return XAPMManager.get().getCurrentTopPackage();
    }
    // INPUT END.


    // AM START
    @FunctionProperties
    public boolean startActivity() {
        return true;
    }
    // AM END.

    // POLICY START.
    @SuppressLint("MissingPermission")
    @FunctionProperties
    public void wifi(boolean on) {
        @SuppressLint("WifiManagerPotentialLeak")
        WifiManager manager = (WifiManager) androidContext
                .getSystemService(android.content.Context.WIFI_SERVICE);
        assert manager != null;
        if (on && !manager.isWifiEnabled()) {
            manager.setWifiEnabled(true);
        }

        if (!on && manager.isWifiEnabled()) {
            manager.setWifiEnabled(true);
        }
    }

    @FunctionProperties
    @SuppressLint("MissingPermission")
    public void bt(boolean on) {
        boolean isOn = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (on && !isOn) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }

        if (!on && isOn) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    @FunctionProperties
    public void gps(boolean on) {

    }

    @FunctionProperties
    public void screenshot() {
        throw new RuntimeException("No impl yet.");
    }
    // POLICY END.
}