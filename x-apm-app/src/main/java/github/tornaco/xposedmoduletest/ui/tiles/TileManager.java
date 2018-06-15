package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.Activity;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import dev.nick.eventbus.utils.ReflectionUtils;
import dev.nick.tiles.tile.Tile;
import github.tornaco.xposedmoduletest.ui.tiles.prop.Disguise;
import github.tornaco.xposedmoduletest.ui.tiles.prop.PackageInstallVerify;
import github.tornaco.xposedmoduletest.ui.tiles.workflow.Workflow;

/**
 * Created by Tornaco on 2018/5/3 13:42.
 * God bless no bug!
 */
public class TileManager {

    private static final Map<String, Class<? extends Tile>> CLAZZ_MAP = new HashMap<>();

    static {
        CLAZZ_MAP.put(getTileKey(AppStart.class), AppStart.class);
        CLAZZ_MAP.put(getTileKey(AppBoot.class), AppBoot.class);
        CLAZZ_MAP.put(getTileKey(RFKill.class), RFKill.class);
        CLAZZ_MAP.put(getTileKey(TRKill.class), TRKill.class);

        CLAZZ_MAP.put(getTileKey(AppGuard.class), AppGuard.class);
        CLAZZ_MAP.put(getTileKey(Blur.class), Blur.class);
        CLAZZ_MAP.put(getTileKey(UnInstall.class), UnInstall.class);
        CLAZZ_MAP.put(getTileKey(Privacy.class), Privacy.class);

        CLAZZ_MAP.put(getTileKey(CompReplacement.class), CompReplacement.class);
        CLAZZ_MAP.put(getTileKey(PermControl.class), PermControl.class);
        CLAZZ_MAP.put(getTileKey(SmartSense.class), SmartSense.class);
        CLAZZ_MAP.put(getTileKey(Greening.class), Greening.class);

        CLAZZ_MAP.put(getTileKey(Lazy.class), Lazy.class);
        CLAZZ_MAP.put(getTileKey(Doze.class), Doze.class);
        CLAZZ_MAP.put(getTileKey(PushMessageHandler.class), PushMessageHandler.class);
        CLAZZ_MAP.put(getTileKey(NFManager.class), NFManager.class);
        CLAZZ_MAP.put(getTileKey(Workflow.class), Workflow.class);

        // Hiddens.
        CLAZZ_MAP.put(getTileKey(Resident.class), Resident.class);
        CLAZZ_MAP.put(getTileKey(Disguise.class), Disguise.class);
        CLAZZ_MAP.put(getTileKey(PackageInstallVerify.class), PackageInstallVerify.class);
    }

    public static String getTileKey(Class<? extends Tile> clazz) {
        return clazz.getSimpleName();
    }

    @Nullable
    public static Tile makeTileByKey(Class<? extends Tile> clazz, Activity activity) {
        Constructor constructor = ReflectionUtils.findConstructor(clazz, activity);
        if (constructor == null) return null;
        try {
            return (Tile) constructor.newInstance(activity);
        } catch (Exception e) {
            Logger.e("Fail makeTileByKey: " + Logger.getStackTraceString(e));
        }
        return null;
    }

    public static Tile makeTileByKey(String key, Activity activity) {
        Class<? extends Tile> clazz = CLAZZ_MAP.get(key);
        return makeTileByKey(clazz, activity);
    }
}
