package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public class RepoProxy {

    private static final Singleton<RepoProxy> sProxy =
            new Singleton<RepoProxy>() {
                @Override
                protected RepoProxy create() {
                    return new RepoProxy();
                }
            };

    public static RepoProxy getProxy() {
        return sProxy.get();
    }

    private SetRepo<String> boots, starts, lks, rfks,
            perms, privacy, greens,
            blurs, locks, uninstall,
            data_restrict, wifi_restrict,
            lazy, comps;

    private MapRepo<String, String> appFocused, appUnFocused;

    private RepoProxy() {

        // Sync in a new handler thread.
        HandlerThread hr = new HandlerThread("Repo proxy");
        hr.start();
        Handler h = new Handler(hr.getLooper());

        ExecutorService io = Executors.newSingleThreadExecutor();

        bringBases(h, io);
        bringUpSenses(h, io);
    }

    private void bringBases(Handler h, ExecutorService io) {
        File systemFile = new File(Environment.getDataDirectory(), "system");
        File dir = new File(systemFile, "tor_apm");
        if (!dir.exists()) {
            dir = new File(systemFile, "tor");
        }

        boots = new StringSetRepo(new File(dir, "boots"), h, io);
        starts = new StringSetRepo(new File(dir, "starts"), h, io);
        lks = new StringSetRepo(new File(dir, "lks"), h, io);
        rfks = new StringSetRepo(new File(dir, "rfks"), h, io);
        comps = new StringSetRepo(new File(dir, "comps"), h, io);
        perms = new StringSetRepo(new File(dir, "perms"), h, io);
        privacy = new StringSetRepo(new File(dir, "privacy"), h, io);
        greens = new StringSetRepo(new File(dir, "greens"), h, io);
        blurs = new StringSetRepo(new File(dir, "blurs"), h, io);
        locks = new StringSetRepo(new File(dir, "locks"), h, io);
        uninstall = new StringSetRepo(new File(dir, "uninstall"), h, io);
        lazy = new StringSetRepo(new File(dir, "lazy"), h, io);

        // FIXME java.io.FileNotFoundException:
        // /data/system/tor/wifi_restrict: open failed: EISDIR (Is a directory)
        if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            data_restrict = new StringSetRepo(new File(dir, "data_restricts_fix"), h, io);
            wifi_restrict = new StringSetRepo(new File(dir, "wifi_restricts_fix"), h, io);
        }
    }

    private void bringUpSenses(Handler h, ExecutorService io) {

        File systemFile = new File(Environment.getDataDirectory(), "system");
        File dir = new File(systemFile, "tor_apm");
        if (!dir.exists()) {
            dir = new File(systemFile, "tor");
        }

        appFocused = new StringMapRepo(new File(dir, "app_focused"), h, io);
        appUnFocused = new StringMapRepo(new File(dir, "app_unfocused"), h, io);
    }

    private static final SetRepo<String> STRING_SET_NULL_HACK = new SetRepo<String>() {
        @Override
        public Set<String> getAll() {
            XposedLog.verbose("getAll element on NULL-HACK");
            return new HashSet<>(0);
        }

        @Override
        public void reload() {

        }

        @Override
        public void reloadAsync() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void flushAsync() {

        }

        @Override
        public boolean add(String s) {
            XposedLog.verbose("add element on NULL-HACK");
            return false;
        }

        @Override
        public boolean remove(String s) {
            XposedLog.verbose("remove element on NULL-HACK");
            return false;
        }

        @Override
        public void removeAll() {
            XposedLog.verbose("removeAll element on NULL-HACK");
        }

        @Override
        public boolean has(String s) {
            XposedLog.verbose("has element on NULL-HACK");
            return false;
        }

        @Override
        public String name() {
            return "NULL-HACK";
        }
    };

    private MapRepo<String, String> MAP_SET_NULL_HACK = new MapRepo<String, String>() {
        @Override
        public void reload() {

        }

        @Override
        public void reloadAsync() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void flushAsync() {

        }

        @Override
        public String name() {
            return "MAP_SET_NULL_HACK";
        }

        @Override
        public int size() {
            XposedLog.verbose("size element on MAP_SET_NULL_HACK");
            return 0;
        }

        @Override
        public boolean isEmpty() {
            XposedLog.verbose("isEmpty element on MAP_SET_NULL_HACK");
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            XposedLog.verbose("containsKey element on MAP_SET_NULL_HACK");
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            XposedLog.verbose("containsValue element on MAP_SET_NULL_HACK");
            return false;
        }

        @Override
        public String get(Object key) {
            XposedLog.verbose("get element on MAP_SET_NULL_HACK");
            return null;
        }

        @Override
        public String put(String key, String value) {
            XposedLog.verbose("put element on MAP_SET_NULL_HACK");
            return null;
        }

        @Override
        public String remove(Object key) {
            XposedLog.verbose("remove element on MAP_SET_NULL_HACK");
            return null;
        }

        @Override
        public void putAll(@NonNull Map<? extends String, ? extends String> m) {
            XposedLog.verbose("putAll element on MAP_SET_NULL_HACK");
        }

        @Override
        public void clear() {
            XposedLog.verbose("size element on MAP_SET_NULL_HACK");
        }

        @NonNull
        @Override
        public Set<String> keySet() {
            XposedLog.verbose("keySet element on MAP_SET_NULL_HACK");
            return new HashSet<>(0);
        }

        @NonNull
        @Override
        public Collection<String> values() {
            XposedLog.verbose("values element on MAP_SET_NULL_HACK");
            return new ArrayList<>(0);
        }

        @NonNull
        @Override
        public Set<Entry<String, String>> entrySet() {
            XposedLog.verbose("entrySet element on MAP_SET_NULL_HACK");
            return new HashSet<>(0);
        }
    };

    public SetRepo<String> getBoots() {
        return boots == null ? STRING_SET_NULL_HACK : boots;
    }

    public SetRepo<String> getStarts() {
        return starts == null ? STRING_SET_NULL_HACK : starts;
    }

    public SetRepo<String> getLks() {
        return lks == null ? STRING_SET_NULL_HACK : lks;
    }

    public SetRepo<String> getRfks() {
        return rfks == null ? STRING_SET_NULL_HACK : rfks;
    }

    public SetRepo<String> getPerms() {
        return perms == null ? STRING_SET_NULL_HACK : perms;
    }

    public SetRepo<String> getPrivacy() {
        return privacy == null ? STRING_SET_NULL_HACK : privacy;
    }

    public SetRepo<String> getGreens() {
        return greens == null ? STRING_SET_NULL_HACK : greens;
    }

    public SetRepo<String> getBlurs() {
        return blurs == null ? STRING_SET_NULL_HACK : blurs;
    }

    public SetRepo<String> getLocks() {
        return locks == null ? STRING_SET_NULL_HACK : locks;
    }

    public SetRepo<String> getUninstall() {
        return uninstall == null ? STRING_SET_NULL_HACK : uninstall;
    }

    public SetRepo<String> getData_restrict() {
        return data_restrict == null ? STRING_SET_NULL_HACK : data_restrict;
    }

    public SetRepo<String> getWifi_restrict() {
        return wifi_restrict == null ? STRING_SET_NULL_HACK : wifi_restrict;
    }

    public SetRepo<String> getLazy() {
        return lazy == null ? STRING_SET_NULL_HACK : lazy;
    }

    public SetRepo<String> getComps() {
        return comps == null ? STRING_SET_NULL_HACK : comps;
    }

    public MapRepo<String, String> getAppFocused() {
        return appFocused == null ? MAP_SET_NULL_HACK : appFocused;
    }

    public MapRepo<String, String> getAppUnFocused() {
        return appUnFocused == null ? MAP_SET_NULL_HACK : appUnFocused;
    }

    public void deleteAll() {
        XposedLog.wtf("deleteAll data...");
        getBoots().removeAll();
        getStarts().removeAll();
        getLks().removeAll();
        getRfks().removeAll();
        getPerms().removeAll();
        getPrivacy().removeAll();
        getGreens().removeAll();
        getBlurs().removeAll();
        getComps().removeAll();
        getLocks().removeAll();
        getUninstall().removeAll();
        getData_restrict().removeAll();
        getWifi_restrict().removeAll();
        getLazy().removeAll();

        getAppFocused().clear();
        getAppUnFocused().clear();
    }
}
