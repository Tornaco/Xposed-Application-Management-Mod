package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.tornaco.android.common.BlackHole;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IBackupAgent;
import github.tornaco.xposedmoduletest.IBackupCallback;
import github.tornaco.xposedmoduletest.IFileDescriptorConsumer;
import github.tornaco.xposedmoduletest.IFileDescriptorInitializer;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.util.ZipUtils;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.DateUtils;
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
            start_rules, lazy_rules, pm_rules,
            ifw_service, ifw_broadcast, ifw_activity,
            lks, rfks, trks,
            perms, privacy, greens, props,
            blurs,
            locks, lock_white_list_activity,
            uninstall,
            data_restrict, wifi_restrict,
            lazy, comps, white_list_hooks_dynamic,
            pending_disable_apps,
            pending_disable_apps_tr,
            resident, doze_whitelist_adding, doze_whitelist_removal,
            wakeup_on_notification;

    private MapRepo<String, String> componentReplacement,
            appFocused, appUnFocused,
            systemPropProfiles,
            appSettingsTemplate, appOpsTemplate,
            js;

    private RepoProxy() {

        // Sync in a new handler thread.
        HandlerThread hr = new HandlerThread("Repo proxy");
        hr.start();
        Handler h = new Handler(hr.getLooper());

        ExecutorService io = Executors.newSingleThreadExecutor();

        bringBases(h, io);
        bringUpMaps(h, io);

        if (BuildConfig.DEBUG) {
            XposedLog.boot("RepoProxy app data dir exists? " + isAppDataDirExists(BuildConfig.APPLICATION_ID));
        }
    }

    private static boolean isAppDataDirExists(String pkgName) {
        try {
            File dataDir = new File("data/data/" + pkgName);
            return dataDir.exists();
        } catch (Throwable e) {
            return false;
        }
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
            BlackHole.eat(f.delete());
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

    public static File getDebugDumpDir() {
        return new File(getBaseDataDir(), "dump");
    }

    public static File getBaseTmpDir() {
        File systemFile = new File(Environment.getDataDirectory(), "system");
        return new File(systemFile, ".tmp_tor");
    }

    public static File getRsCacheDir() {
        return new File(getBaseDataDir(), ".rs_cache");
    }

    public static File getSystemErrorTraceDirByVersion() {
        String versionName = String.valueOf(BuildConfig.VERSION_CODE);
        return new File(getSystemErrorTraceDir(), versionName);
    }

    public static File getDebugDumpDirByVersion() {
        String versionName = String.valueOf(BuildConfig.VERSION_CODE);
        return new File(getDebugDumpDir(), versionName);
    }

    private void bringBases(Handler h, ExecutorService io) {

        File dir = getBaseDataDir();

        boots = new StringSetRepo2(new File(dir, "boots.xml"), h, io);
        migrateSetRepo(h, "boots", boots);

        starts = new StringSetRepo2(new File(dir, "starts.xml"), h, io);
        migrateSetRepo(h, "starts", starts);

        start_rules = new StringSetRepo2(new File(dir, "start_rules.xml"), h, io);
        migrateSetRepo(h, "start_rules", start_rules);

        ifw_activity = new StringSetRepo2(new File(dir, "ifw_activity.xml"), h, io);
        migrateSetRepo(h, "ifw_activity", ifw_activity);

        ifw_broadcast = new StringSetRepo2(new File(dir, "ifw_broadcast.xml"), h, io);
        migrateSetRepo(h, "ifw_broadcast", ifw_broadcast);

        ifw_service = new StringSetRepo2(new File(dir, "ifw_service.xml"), h, io);
        migrateSetRepo(h, "ifw_service", ifw_service);

        lks = new StringSetRepo2(new File(dir, "lks.xml"), h, io);
        migrateSetRepo(h, "lks", lks);

        rfks = new StringSetRepo2(new File(dir, "rfks.xml"), h, io);
        migrateSetRepo(h, "rfks", rfks);

        trks = new StringSetRepo2(new File(dir, "trks.xml"), h, io);
        migrateSetRepo(h, "trks", trks);

        comps = new StringSetRepo2(new File(dir, "comps.xml"), h, io);
        migrateSetRepo(h, "comps", comps);

        perms = new StringSetRepo2(new File(dir, "perms.xml"), h, io);
        migrateSetRepo(h, "perms", perms);

        privacy = new StringSetRepo2(new File(dir, "privacy.xml"), h, io);
        migrateSetRepo(h, "privacy", privacy);

        props = new StringSetRepo2(new File(dir, "props.xml"), h, io);
        migrateSetRepo(h, "props", props);

        greens = new StringSetRepo2(new File(dir, "greens.xml"), h, io);
        migrateSetRepo(h, "greens", greens);

        blurs = new StringSetRepo2(new File(dir, "blurs.xml"), h, io);
        migrateSetRepo(h, "blurs", blurs);

        locks = new StringSetRepo2(new File(dir, "locks.xml"), h, io);
        migrateSetRepo(h, "locks", locks);

        lock_white_list_activity = new StringSetRepo2(new File(dir, "lock_white_list_activity.xml"), h, io);
        migrateSetRepo(h, "lock_white_list_activity", lock_white_list_activity);

        uninstall = new StringSetRepo2(new File(dir, "uninstall.xml"), h, io);
        migrateSetRepo(h, "uninstall", uninstall);

        lazy = new StringSetRepo2(new File(dir, "lazy.xml"), h, io);
        migrateSetRepo(h, "lazy", lazy);

        lazy_rules = new StringSetRepo2(new File(dir, "lazy_rules.xml"), h, io);
        migrateSetRepo(h, "lazy_rules", lazy_rules);

        pm_rules = new StringSetRepo2(new File(dir, "pm_rules.xml"), h, io);
        migrateSetRepo(h, "pm_rules", pm_rules);

        pending_disable_apps = new StringSetRepo2(new File(dir, "pending_disable_apps.xml"), h, io);
        migrateSetRepo(h, "pending_disable_apps", pending_disable_apps);

        pending_disable_apps_tr = new StringSetRepo2(new File(dir, "pending_disable_apps_tr.xml"), h, io);
        migrateSetRepo(h, "pending_disable_apps_tr", pending_disable_apps_tr);

        resident = new StringSetRepo2(new File(dir, "resident.xml"), h, io);
        migrateSetRepo(h, "resident", resident);

        doze_whitelist_adding = new StringSetRepo2(new File(dir, "doze_whitelist_adding.xml"), h, io);
        migrateSetRepo(h, "doze_whitelist_adding", doze_whitelist_adding);

        doze_whitelist_removal = new StringSetRepo2(new File(dir, "doze_whitelist_removal.xml"), h, io);
        migrateSetRepo(h, "doze_whitelist_removal", doze_whitelist_removal);

        wakeup_on_notification = new StringSetRepo2(new File(dir, "wakeup_on_notification.xml"), h, io);
        migrateSetRepo(h, "wakeup_on_notification", wakeup_on_notification);

        // Prevent some system app being added to whitelist.
        white_list_hooks_dynamic = new StringSetRepo2(new File(dir, "white_list_hooks_dynamic.xml"), h, io);
        migrateSetRepo(h, "white_list_hooks_dynamic", white_list_hooks_dynamic);

        // FIXME java.io.FileNotFoundException:
        // /data/system/tor/wifi_restrict: open failed: EISDIR (Is a directory)
        if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            data_restrict = new StringSetRepo2(new File(dir, "data_restricts_fix.xml"), h, io);
            wifi_restrict = new StringSetRepo2(new File(dir, "wifi_restricts_fix.xml"), h, io);

            migrateSetRepo(h, "data_restricts_fix", data_restrict);
            migrateSetRepo(h, "wifi_restricts_fix", wifi_restrict);
        }
    }

    private void bringUpMaps(Handler h, ExecutorService io) {

        File dir = getBaseDataDir();

        componentReplacement = new StringMapRepo2(new File(dir, "component_replacement.xml"), h, io);

        appSettingsTemplate = new StringMapRepo2(new File(dir, "app_settings_template.xml"), h, io);

        appFocused = new StringMapRepo2(new File(dir, "app_focused.xml"), h, io);
        appUnFocused = new StringMapRepo2(new File(dir, "app_unfocused.xml"), h, io);

        systemPropProfiles = new StringMapRepo2(new File(dir, "system_prop_profiles.xml"), h, io);

        appOpsTemplate = new StringMapRepo2(new File(dir, "app_ops_template.xml"), h, io);

        js = new StringMapRepo2(new File(dir, "js.xml"), h, io);

        // Migrate.
        migrateMapRepo(h, "component_replacement", componentReplacement);
        migrateMapRepo(h, "app_settings_template", appSettingsTemplate);
        migrateMapRepo(h, "app_focused", appFocused);
        migrateMapRepo(h, "app_unfocused", appUnFocused);
        migrateMapRepo(h, "system_prop_profiles", systemPropProfiles);
        migrateMapRepo(h, "app_ops_template", appOpsTemplate);
        migrateMapRepo(h, "js", js);
    }

    private void migrateMapRepo(Handler h, String name, Map<String, String> dest) {
        File dir = getBaseDataDir();
        File file = new File(dir, name);
        if (file.exists()) {
            try {
                XposedLog.wtf("Migrating " + name);
                @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                StringMapRepo mapRepo = new StringMapRepo(file, h, null);
                dest.putAll(mapRepo.dup());
            } catch (Throwable e) {
                XposedLog.wtf("Fail migrateMapRepo " + Log.getStackTraceString(e));
            } finally {
                BlackHole.eat(file.delete());
            }
        }
    }

    private void migrateSetRepo(Handler h, String name, SetRepo<String> dest) {
        File dir = getBaseDataDir();
        File file = new File(dir, name);
        if (file.exists()) {
            try {
                XposedLog.wtf("Migrating " + name);
                StringSetRepo repo = new StringSetRepo(file, h, null);
                dest.addAll(repo.getAll());
            } catch (Throwable e) {
                XposedLog.wtf("Fail migrateSetRepo " + Log.getStackTraceString(e));
            } finally {
                BlackHole.eat(file.delete());
            }
        }
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
        public boolean addAll(Collection<? extends String> c) {
            XposedLog.verbose("addAll element on NULL-HACK");
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

    public SetRepo<String> getLazy_rules() {
        return lazy_rules == null ? STRING_SET_NULL_HACK : lazy_rules;
    }

    public SetRepo<String> getPm_rules() {
        return pm_rules == null ? STRING_SET_NULL_HACK : pm_rules;
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

    public SetRepo<String> getProps() {
        return props == null ? STRING_SET_NULL_HACK : props;
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

    public SetRepo<String> getWakeup_on_notification() {
        return wakeup_on_notification == null ? STRING_SET_NULL_HACK : wakeup_on_notification;
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

    public MapRepo<String, String> getSystemPropProfiles() {
        return systemPropProfiles == null ? MAP_SET_NULL_HACK : systemPropProfiles;
    }

    public MapRepo<String, String> getJs() {
        return js == null ? MAP_SET_NULL_HACK : js;
    }

    public MapRepo<String, String> getAppOpsTemplate() {
        return appOpsTemplate == null ? MAP_SET_NULL_HACK : appOpsTemplate;
    }

    public MapRepo<String, String> getAppSettingsTemplate() {
        return appSettingsTemplate == null ? MAP_SET_NULL_HACK : appSettingsTemplate;
    }

    public IBackupAgent getBackupAgent() {
        return new BackupAgentBinder();
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
        getProps().removeAll();
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
        getWakeup_on_notification().removeAll();

        getStart_rules().removeAll();
        getLazy_rules().removeAll();
        getPm_rules().removeAll();

        getIfw_activity().removeAll();
        getIfw_broadcast().removeAll();
        getIfw_service().removeAll();

        getAppFocused().clear();
        getAppUnFocused().clear();
        getComponentReplacement().clear();
        getSystemPropProfiles().clear();
        getJs().clear();
        getAppOpsTemplate().clear();
        getAppSettingsTemplate().clear();

        // Reset all settings.
        SettingsProvider.get().reset();
    }

    private static class BackupAgentBinder extends IBackupAgent.Stub {

        @Override
        public void performBackup(IFileDescriptorInitializer initializer,
                                  String domain, String path, IBackupCallback callback)
                throws RemoteException {
            if (initializer == null) {
                callback.onFail("IFileDescriptorInitializer is null");
                return;
            }

            // Create tmp dir.
            File tmpDir = getBaseTmpDir();

            try {
                Files.createParentDirs(tmpDir);
                XposedLog.wtf("IBackupAgent, tmpDir: " + tmpDir);
            } catch (IOException e) {
                callback.onFail(e.getLocalizedMessage());
                XposedLog.wtf("IBackupAgent, createParentDirs fail deleteDirQuiet : " + tmpDir);
                XposedLog.wtf("IBackupAgent, createParentDirs fail : " + Log.getStackTraceString(e));
                return;
            }
            // Zip all subFiles.
            long startTimeMills = System.currentTimeMillis();
            String name = "X-APM-Backup-" + DateUtils.formatForFileName(startTimeMills) + ".zip";
            try {
                ZipUtils.zip(getBaseDataDir().getAbsolutePath(), tmpDir.getAbsolutePath(), name);
                File zipFile = new File(tmpDir, name);
                XposedLog.wtf("IBackupAgent, zipFile: " + zipFile);
                String relativePath = toRelativePath(zipFile);
                // Init pfd.
                initializer.initParcelFileDescriptor(relativePath, relativePath,
                        new IFileDescriptorConsumer.Stub() {
                            @Override
                            public void acceptAppParcelFileDescriptor(ParcelFileDescriptor pfd) throws RemoteException {
                                try {
                                    if (pfd == null) {
                                        callback.onFail("IBackupAgent ParcelFileDescriptor is null");
                                        return;
                                    }
                                    Files.asByteSource(zipFile)
                                            .copyTo(new FileOutputStream(pfd.getFileDescriptor()));
                                    XposedLog.wtf("IBackupAgent, performBackup subFile complete: " + zipFile);
                                    callback.onProgress(zipFile.getName());
                                    callback.onBackupFinished(domain, relativePath);
                                } catch (IOException e) {
                                    XposedLog.wtf("IBackupAgent, IOException performBackup subFile: " + Log.getStackTraceString(e));
                                    callback.onFail(e.getLocalizedMessage());
                                    XposedLog.wtf("IBackupAgent, acceptAppParcelFileDescriptor fail : " + Log.getStackTraceString(e));
                                } finally {
                                    FileUtil.deleteDirQuiet(tmpDir);
                                    Closer.closeQuietly(pfd);
                                    XposedLog.wtf("IBackupAgent, deleteDirQuiet : " + tmpDir);
                                }
                            }
                        });
            } catch (Exception e) {
                callback.onFail(e.getLocalizedMessage());
                FileUtil.deleteDirQuiet(tmpDir);
                XposedLog.wtf("IBackupAgent, backup fail : " + Log.getStackTraceString(e));
                XposedLog.wtf("IBackupAgent, deleteDirQuiet : " + tmpDir);
            }
        }

        @Override
        public void performRestore(ParcelFileDescriptor pfd, String domain, String path, IBackupCallback callback) throws RemoteException {
            if (pfd == null) {
                callback.onFail("IBackupAgent ParcelFileDescriptor is null");
                return;
            }
            // Create tmp dir.
            File tmpDir = getBaseTmpDir();
            File tmpZipFile = new File(tmpDir, "restore_file.zip");
            XposedLog.wtf("IBackupAgent, zipFile : " + tmpZipFile);
            // Copy to tmp.zip.
            try {
                Files.createParentDirs(tmpZipFile);
                Files.asByteSink(tmpZipFile)
                        .writeFrom(new FileInputStream(pfd.getFileDescriptor()));
            } catch (IOException e) {
                XposedLog.wtf("IBackupAgent, IOException copy zip to tmp: " + Log.getStackTraceString(e));
                callback.onFail(e.getLocalizedMessage());
                return;
            } finally {
                Closer.closeQuietly(pfd);
            }
            try {
                ZipUtils.unzip(tmpZipFile.getAbsolutePath(), getBaseDataDir().getAbsolutePath(), false);
                callback.onRestoreFinished(domain, path);
            } catch (Exception e) {
                XposedLog.wtf("IBackupAgent, IOException unzip to tmp: " + Log.getStackTraceString(e));
                callback.onFail(e.getLocalizedMessage());
            } finally {
                FileUtil.deleteDirQuiet(tmpDir);
            }
        }

        private static String toRelativePath(File subFile) {
            File dataDir = getBaseDataDir();
            return subFile.getAbsolutePath().replace(dataDir.getAbsolutePath(), "");
        }
    }
}
