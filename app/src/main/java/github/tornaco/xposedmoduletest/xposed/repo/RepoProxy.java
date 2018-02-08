package github.tornaco.xposedmoduletest.xposed.repo;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
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

    private SetRepo<String>
            boots,
            starts,
            start_rules,
            ifw_service, ifw_broadcast, ifw_activity,
            lks, rfks, trks,
            perms, privacy, greens,
            blurs,
            locks, lock_white_list_activity,
            uninstall,
            data_restrict, wifi_restrict,
            lazy, comps, white_list_hooks_dynamic,
            pending_disable_apps,
            pending_disable_apps_tr,
            resident, doze_whitelist_adding, doze_whitelist_removal;

    private MapRepo<String, String> componentReplacement, appFocused, appUnFocused;

    private Handler h;

    private RepoProxy() {

        // Sync in a new handler thread.
        HandlerThread hr = new HandlerThread("Repo proxy");
        hr.start();
        h = new Handler(hr.getLooper());

        ExecutorService io = Executors.newSingleThreadExecutor();

        bringBases(h, io);
        bringUpMaps(h, io);
    }

    public static void createFileIndicator(String name) {
        File f = new File(getBaseDataDir(), name);
        try {
            Files.createParentDirs(f);
        } catch (Exception e) {
            XposedLog.wtf("Fail mk dir when createFileIndicator " + Log.getStackTraceString(e));
        }
        try {
            Files.touch(f);
        } catch (IOException e) {
            XposedLog.wtf("Fail createFileIndicator " + Log.getStackTraceString(e));
        }
    }

    public static void deleteFileIndicator(String name) {
        File f = new File(getBaseDataDir(), name);
        try {
            f.delete();
        } catch (Exception e) {
            XposedLog.wtf("Fail deleteFileIndicator " + Log.getStackTraceString(e));
        }
    }

    public static boolean hasFileIndicator(String name) {
        File f = new File(getBaseDataDir(), name);
        return f.exists();
    }

    public static File getBaseDataDir() {
        File systemFile = new File(Environment.getDataDirectory(), "system");
        File dir = new File(systemFile, "tor_apm");
        if (!dir.exists()) {
            dir = new File(systemFile, "tor");
        }
        return dir;
    }

    public static File getSystemErrorTraceDir() {
        return new File(getBaseDataDir(), "trace");
    }

    public static File getSystemErrorTraceDirByVersion() {
        String versionName = String.valueOf(BuildConfig.VERSION_CODE);
        return new File(getSystemErrorTraceDir(), versionName);
    }

    private static void cleanUpBaseDataDir() {
        try {
            FileUtil.deleteDir(getBaseDataDir());
        } catch (Throwable e) {
            XposedLog.wtf("Fail clean up dir: " + e);
        }
    }

    private void bringBases(Handler h, ExecutorService io) {

        File dir = getBaseDataDir();

        boots = new StringSetRepo(new File(dir, "boots"), h, io);
        starts = new StringSetRepo(new File(dir, "starts"), h, io);
        start_rules = new StringSetRepo(new File(dir, "start_rules"), h, io);
        ifw_activity = new StringSetRepo(new File(dir, "ifw_activity"), h, io);
        ifw_broadcast = new StringSetRepo(new File(dir, "ifw_broadcast"), h, io);
        ifw_service = new StringSetRepo(new File(dir, "ifw_service"), h, io);
        lks = new StringSetRepo(new File(dir, "lks"), h, io);
        rfks = new StringSetRepo(new File(dir, "rfks"), h, io);
        trks = new StringSetRepo(new File(dir, "trks"), h, io);
        comps = new StringSetRepo(new File(dir, "comps"), h, io);
        perms = new StringSetRepo(new File(dir, "perms"), h, io);
        privacy = new StringSetRepo(new File(dir, "privacy"), h, io);
        greens = new StringSetRepo(new File(dir, "greens"), h, io);
        blurs = new StringSetRepo(new File(dir, "blurs"), h, io);
        locks = new StringSetRepo(new File(dir, "locks"), h, io);
        lock_white_list_activity = new StringSetRepo(new File(dir, "lock_white_list_activity"), h, io);
        uninstall = new StringSetRepo(new File(dir, "uninstall"), h, io);
        lazy = new StringSetRepo(new File(dir, "lazy"), h, io);
        pending_disable_apps = new StringSetRepo(new File(dir, "pending_disable_apps"), h, io);
        pending_disable_apps_tr = new StringSetRepo(new File(dir, "pending_disable_apps_tr"), h, io);
        resident = new StringSetRepo(new File(dir, "resident"), h, io);
        doze_whitelist_adding = new StringSetRepo(new File(dir, "doze_whitelist_adding"), h, io);
        doze_whitelist_removal = new StringSetRepo(new File(dir, "doze_whitelist_removal"), h, io);

        if (BuildConfig.DEBUG) try {
            @SuppressLint("SdCardPath") File dynamicHooks =
                    new File("/sdcard/.android/.apm_configs"
                            + File.separator
                            + ".apm_white_list_hooks");
            white_list_hooks_dynamic = new StringSetRepo(dynamicHooks, h, io);
        } catch (Exception e) {
            XposedLog.wtf("Fail init white list hooks " + e);
        }

        // FIXME java.io.FileNotFoundException:
        // /data/system/tor/wifi_restrict: open failed: EISDIR (Is a directory)
        if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            data_restrict = new StringSetRepo(new File(dir, "data_restricts_fix"), h, io);
            wifi_restrict = new StringSetRepo(new File(dir, "wifi_restricts_fix"), h, io);
        }
    }

    private void bringUpMaps(Handler h, ExecutorService io) {

        File dir = getBaseDataDir();

        appFocused = new StringMapRepo(new File(dir, "app_focused"), h, io);
        appUnFocused = new StringMapRepo(new File(dir, "app_unfocused"), h, io);
        componentReplacement = new StringMapRepo(new File(dir, "component_replacement"), h, io);
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
        public int size() {
            return 0;
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
        public boolean has(String[] t) {
            XposedLog.verbose("has element[] on NULL-HACK");
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
        public Map<String, String> dup() {
            return new HashMap<>(0);
        }

        @Override
        public boolean hasNoneNullValue(String s) {
            return false;
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

    public SetRepo<String> getStart_rules() {
        return start_rules == null ? STRING_SET_NULL_HACK : start_rules;
    }

    public SetRepo<String> getIfw_activity() {
        return ifw_activity == null ? STRING_SET_NULL_HACK : ifw_activity;
    }

    public SetRepo<String> getIfw_broadcast() {
        return ifw_broadcast == null ? STRING_SET_NULL_HACK : ifw_broadcast;
    }

    public SetRepo<String> getIfw_service() {
        return ifw_service == null ? STRING_SET_NULL_HACK : ifw_service;
    }

    public SetRepo<String> getLks() {
        return lks == null ? STRING_SET_NULL_HACK : lks;
    }

    public SetRepo<String> getRfks() {
        return rfks == null ? STRING_SET_NULL_HACK : rfks;
    }

    public SetRepo<String> getTrks() {
        return trks == null ? STRING_SET_NULL_HACK : trks;
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

    public SetRepo<String> getLock_white_list_activity() {
        return lock_white_list_activity == null ? STRING_SET_NULL_HACK : lock_white_list_activity;
    }

    public SetRepo<String> getDoze_whitelist_adding() {
        return doze_whitelist_adding == null ? STRING_SET_NULL_HACK : doze_whitelist_adding;
    }

    public SetRepo<String> getDoze_whitelist_removal() {
        return doze_whitelist_removal == null ? STRING_SET_NULL_HACK : doze_whitelist_removal;
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

    public SetRepo<String> getWhite_list_hooks_dynamic() {
        return white_list_hooks_dynamic == null ? STRING_SET_NULL_HACK : white_list_hooks_dynamic;
    }

    public SetRepo<String> getPending_disable_apps() {
        return pending_disable_apps == null ? STRING_SET_NULL_HACK : pending_disable_apps;
    }

    public SetRepo<String> getPending_disable_apps_tr() {
        return pending_disable_apps_tr == null ? STRING_SET_NULL_HACK : pending_disable_apps_tr;
    }

    public SetRepo<String> getResident() {
        return resident == null ? STRING_SET_NULL_HACK : resident;
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

    public MapRepo<String, String> getComponentReplacement() {
        return componentReplacement == null ? MAP_SET_NULL_HACK : componentReplacement;
    }

    public void backupTo(final String dir) throws RemoteException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                XposedLog.verbose("backupTo: " + dir);
                final File dirFile = new File(dir);

                try {
                    Files.createParentDirs(new File(dirFile, "dummy"));
                } catch (IOException e) {
                    XposedLog.wtf("backupTo fail: create dir fail");
                    return;
                }

                if (!dirFile.exists()) {
                    XposedLog.wtf("backupTo fail: target dir not exists");
                    return;
                }

                File systemFile = new File(Environment.getDataDirectory(), "system");
                File sourceDir = new File(systemFile, "tor_apm");
                if (!sourceDir.exists()) {
                    sourceDir = new File(systemFile, "tor");
                }

                github.tornaco.android.common.Collections.consumeRemaining(Files.fileTreeTraverser()
                        .postOrderTraversal(sourceDir), new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        try {
                            Files.copy(file, new File(dirFile, file.getName()));
                        } catch (IOException e) {
                            XposedLog.wtf("Fail copy file: " + Log.getStackTraceString(e));
                        }
                    }
                });
            }
        };
        h.post(r);
    }

    public void restoreFrom(String dir) throws RemoteException {

    }

    public void deleteAll() {
        XposedLog.wtf("deleteAll data...");
        getBoots().removeAll();
        getStarts().removeAll();
        getLks().removeAll();
        getRfks().removeAll();
        getTrks().removeAll();
        getPerms().removeAll();
        getPrivacy().removeAll();
        getGreens().removeAll();
        getBlurs().removeAll();
        getComps().removeAll();
        getLocks().removeAll();
        getLock_white_list_activity().removeAll();
        getUninstall().removeAll();
        getData_restrict().removeAll();
        getWifi_restrict().removeAll();
        getLazy().removeAll();
        getPending_disable_apps().removeAll();
        getPending_disable_apps_tr().removeAll();
        getResident().removeAll();
        getDoze_whitelist_adding().removeAll();
        getDoze_whitelist_removal().removeAll();

        getStart_rules().removeAll();
        getIfw_activity().removeAll();
        getIfw_broadcast().removeAll();
        getIfw_service().removeAll();

        getAppFocused().clear();
        getAppUnFocused().clear();
        getComponentReplacement().clear();
    }
}
