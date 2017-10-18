package dev.tornaco.vangogh;

import org.newstand.logger.Logger;

import java.util.Observable;

import lombok.Getter;

/**
 * Created by guohao4 on 2017/8/29.
 * Email: Tornaco@163.com
 */

public class VangoghConfigManager extends Observable {

    @Getter
    private VangoghConfig config;

    private static VangoghConfigManager sMe = new VangoghConfigManager();

    public static VangoghConfigManager getInstance() {
        return sMe;
    }

    void updateConfig(VangoghConfig newConfig) {
        if (config == null) {
            config = newConfig;
            return;
        }
        if (config.equals(newConfig)) return;

        Logger.v("VangoghConfigManager, config changed to: %s", newConfig);

        config = newConfig;
        setChanged();
        notifyObservers(newConfig);
    }
}
