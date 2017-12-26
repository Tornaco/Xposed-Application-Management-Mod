package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.util.HashSet;
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

    private RepoProxy() {
        HandlerThread hr = new HandlerThread("Repo proxy");
        hr.start();
        Handler h = new Handler(hr.getLooper());

        ExecutorService io = Executors.newSingleThreadExecutor();

        File systemFile = new File(Environment.getDataDirectory(), "system");
        File dir = new File(systemFile, "tor");

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

    private static final SetRepo<String> NULL_HACK = new SetRepo<String>() {
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

    public SetRepo<String> getBoots() {
        return boots == null ? NULL_HACK : boots;
    }

    public SetRepo<String> getStarts() {
        return starts == null ? NULL_HACK : starts;
    }

    public SetRepo<String> getLks() {
        return lks == null ? NULL_HACK : lks;
    }

    public SetRepo<String> getRfks() {
        return rfks == null ? NULL_HACK : rfks;
    }

    public SetRepo<String> getPerms() {
        return perms == null ? NULL_HACK : perms;
    }

    public SetRepo<String> getPrivacy() {
        return privacy == null ? NULL_HACK : privacy;
    }

    public SetRepo<String> getGreens() {
        return greens == null ? NULL_HACK : greens;
    }

    public SetRepo<String> getBlurs() {
        return blurs == null ? NULL_HACK : blurs;
    }

    public SetRepo<String> getLocks() {
        return locks == null ? NULL_HACK : locks;
    }

    public SetRepo<String> getUninstall() {
        return uninstall == null ? NULL_HACK : uninstall;
    }

    public SetRepo<String> getData_restrict() {
        return data_restrict == null ? NULL_HACK : data_restrict;
    }

    public SetRepo<String> getWifi_restrict() {
        return wifi_restrict == null ? NULL_HACK : wifi_restrict;
    }

    public SetRepo<String> getLazy() {
        return lazy == null ? NULL_HACK : lazy;
    }

    public SetRepo<String> getComps() {
        return comps == null ? NULL_HACK : comps;
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
    }
}
