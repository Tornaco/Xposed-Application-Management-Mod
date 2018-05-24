package github.tornaco.xposedmoduletest.xposed.service.input;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

public class KeyEventSender {

    public static boolean injectInputEvent(InputEvent event, int mode) {
        XposedLog.verbose("injectInputEvent: " + event);
        InputManager inputManager = InputManager.getInstance();
        try {
            return (boolean) XposedHelpers.callMethod(inputManager,
                    "injectInputEvent", event, mode);
        } catch (Throwable e) {
            XposedLog.wtf("Fail injectInputEvent: " + event
                    + ", error: " + Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean injectKey(int code) {
        int flags = KeyEvent.FLAG_FROM_SYSTEM;
        int scancode = 12;
        final long eventTime = SystemClock.uptimeMillis();
        KeyEvent down = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, scancode, flags, InputDevice.SOURCE_UNKNOWN);
        KeyEvent up = new KeyEvent(eventTime + 10, eventTime + 10, KeyEvent.ACTION_UP, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, scancode, flags, InputDevice.SOURCE_UNKNOWN);
        return injectInputEvent(down, 0) && injectInputEvent(up, 0);
    }

    public static boolean injectHomeKey() {
        return injectKey(KeyEvent.KEYCODE_HOME);
    }

    public static boolean injectPowerKey() {
        return injectKey(KeyEvent.KEYCODE_POWER);
    }
}
