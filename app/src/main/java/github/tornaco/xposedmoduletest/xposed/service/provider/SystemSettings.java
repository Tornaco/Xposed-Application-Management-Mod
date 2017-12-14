package github.tornaco.xposedmoduletest.xposed.service.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;

import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public enum SystemSettings implements NameValueReader, NameValueWriter, UriProvider, ContentObservable {

    APP_GUARD_ENABLED_B(0) {
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
    },

    LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B(1) {
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
    },

    ASH_CONTROL_MODE_I(XAshmanManager.ControlMode.BLACK_LIST) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            int mode = (int) value;
            return resolver != null && Settings.System.putInt(resolver, name(), mode);
        }

        @Override
        public Object readFromSystemSettings(Context context) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) return getDefValue();
            int def = getDefValue();
            return Settings.System.getInt(resolver, name(), def);
        }
    },

    AUTO_BLACK_FOR_NEW_INSTALLED_APP_B(1) {
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
    },

    LOCK_KILL_DELAY_L(0L) {
        @Override
        public boolean writeToSystemSettings(Context context, Object value) {
            ContentResolver resolver = context.getContentResolver();
            long delay = (long) value;
            return resolver != null && Settings.System.putLong(resolver, name(), delay);
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
}


interface NameValueReader {
    Object readFromSystemSettings(Context context);
}

interface NameValueWriter {
    boolean writeToSystemSettings(Context context, Object value);
}

interface UriProvider {
    Uri getUri();
}

interface ContentObservable {
    void observe(Context context, boolean notifyForDescendants, ContentObserver observer);
}
