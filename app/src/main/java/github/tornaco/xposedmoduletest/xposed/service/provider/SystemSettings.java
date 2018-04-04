package github.tornaco.xposedmoduletest.xposed.service.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsKey;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public enum SystemSettings implements NameValueReader, NameValueWriter, UriProvider, ContentObservable {

    APM_POWER_SAVE_B(1) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }
    },

    APP_GUARD_ENABLED_NEW_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }
    },

    APP_GUARD_DEBUG_MODE_B_S(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    UNINSTALL_GUARD_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    BLUR_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    BLUR_RADIUS_I(BlurSettings.BLUR_RADIUS) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            int v = (int) value;
            return resolver != null && Settings.System.putInt(resolver, name(), v);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def);
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def);
        }
    },

    LOCK_KILL_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    GREENING_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    PRIVACY_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    LAZY_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    ASH_WHITE_SYS_APP_ENABLED_B(1) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    ASH_WONT_KILL_SBN_APP_B(1) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    ASH_WONT_KILL_SBN_APP_GREEN_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    ROOT_ACTIVITY_KILL_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    REMOVE_TASK_KILL_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    LONG_PRESS_BACK_KILL_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    COMP_SETTING_BLOCK_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    BOOT_BLOCK_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    START_BLOCK_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    INTERRUPT_FP_SUCCESS_VB_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    INTERRUPT_FP_ERROR_VB_ENABLED_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    ASH_CONTROL_MODE_I(XAshmanManager.ControlMode.BLACK_LIST) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            int mode = (int) value;
            return resolver != null && Settings.System.putInt(resolver, name(), mode);
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def);
        }
    },

    AUTO_BLACK_FOR_NEW_INSTALLED_APP_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    AUTO_BLACK_NOTIFICATION_FOR_NEW_INSTALLED_APP_B(1) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    DATA_MIGRATE_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    PERMISSION_CONTROL_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },


    SHOW_FOCUSED_ACTIVITY_INFO_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    SHOW_CRASH_DUMP_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    NETWORK_RESTRICT_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_DOZE_ENABLE_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_FORCE_DOZE_ENABLE_B(1) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_DISABLE_MOTION_ENABLE_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_RESIDENT_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_PANIC_LOCK_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_PANIC_HOME_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    APM_SHOW_APP_PROCESS_UPDATE_B(0) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            boolean enabled = (boolean) value;
            return resolver != null && Settings.System.putInt(resolver, name(), enabled ? 1 : 0);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def) == 1;
        }

        @Override
        public void restoreDef(Context context) {
            int def = getDefValue();
            writeToSystemSettings(context, def == 1);
        }
    },

    USER_DEFINED_LINE1_NUM_T_S("18888888888") {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            return resolver != null && Settings.System.putString(resolver, name(), String.valueOf(value));
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            return Settings.System.getString(resolver, name());
        }

        @Override
        public void restoreDef(Context context) {
            String def = getDefValue();
            writeToSystemSettings(context, def);
        }
    },

    USER_DEFINED_DEVICE_ID_T_S("fuckit") {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            return resolver != null && Settings.System.putString(resolver, name(), String.valueOf(value));
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            return Settings.System.getString(resolver, name());
        }

        @Override
        public void restoreDef(Context context) {
            String def = getDefValue();
            writeToSystemSettings(context, def);
        }
    },

    USER_DEFINED_ANDROID_ID_T_S("xxxxxxxxxx") {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            return resolver != null && Settings.System.putString(resolver, name(), String.valueOf(value));
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            return Settings.System.getString(resolver, name());
        }

        @Override
        public void restoreDef(Context context) {
            String def = getDefValue();
            writeToSystemSettings(context, def);
        }
    },

    DOZE_DELAY_L(5 * 60 * 1000L) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            long delay = (long) value;
            return resolver != null && Settings.System.putLong(resolver, name(), delay);
        }

        @Override
        public void restoreDef(Context context) {
            long def = getDefValue();
            writeToSystemSettings(context, def);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            long def = getDefValue();
            return Settings.System.getLong(resolver, name(), def);
        }
    },

    LOCK_KILL_DELAY_L(0L) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            long delay = (long) value;
            return resolver != null && Settings.System.putLong(resolver, name(), delay);
        }

        @Override
        public void restoreDef(Context context) {
            long def = getDefValue();
            writeToSystemSettings(context, def);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            long def = getDefValue();
            return Settings.System.getLong(resolver, name(), def);
        }
    };


    private Object defValue;

    SystemSettings(Object defValue) {
        this.defValue = defValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefValue() {
        return (T) defValue;
    }

    @Override
    public Uri getUri() {
        return Settings.System.getUriFor(name());
    }

    @Override
    public void observe(Context context, boolean notifyForDescendants, ContentObserver observer) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) return;
        resolver.registerContentObserver(getUri(), notifyForDescendants, observer);
    }

    public static void moveToNewSettings(Context context) {
        try {
            for (SystemSettings s : values()) {
                Object v = s.readFromSystemSettings(context);
                String key = s.name();
                SettingsProvider.get().putString(key, String.valueOf(v));
            }

            SettingsProvider.get().putBoolean(SettingsKey.SETTINGS_MERGED, true);
        } catch (Throwable e) {
            XposedLog.wtf("Error moveToNewSettings: " + Log.getStackTraceString(e));
        }
    }

    public static void restoreDefault(Context context) {
        try {
            for (SystemSettings s : values()) {
                s.restoreDef(context);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Error restoreDefault: " + Log.getStackTraceString(e));
        }
    }
}


interface NameValueReader {
    Object readFromSystemSettings(Context context);
}

interface NameValueWriter {
    boolean writeToSystemSettings(Context context, Object value);

    void restoreDef(Context context);
}

interface UriProvider {
    Uri getUri();
}

interface ContentObservable {
    void observe(Context context, boolean notifyForDescendants, ContentObserver observer);
}
