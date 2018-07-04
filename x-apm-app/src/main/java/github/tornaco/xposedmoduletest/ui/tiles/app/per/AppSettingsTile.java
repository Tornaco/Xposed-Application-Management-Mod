package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.tiles.tile.QuickTile;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */
public class AppSettingsTile extends QuickTile {

    @Getter
    protected AppSettings appSettings;

    public AppSettingsTile(@NonNull Context context, AppSettings appSettings) {
        super(context, null);
        this.appSettings = appSettings;
    }
}
