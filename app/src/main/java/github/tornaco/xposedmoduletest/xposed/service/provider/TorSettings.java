package github.tornaco.xposedmoduletest.xposed.service.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public enum TorSettings implements NameValueReader, NameValueWriter, UriProvider, ContentObservable {

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

    APP_GUARD_DEBUG_MODE_B(0) {
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
    };


    private Object defValue;

    TorSettings(Object defValue) {
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
