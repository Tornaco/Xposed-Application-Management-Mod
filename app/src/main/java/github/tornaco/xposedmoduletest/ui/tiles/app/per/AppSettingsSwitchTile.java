package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */

public class AppSettingsSwitchTile extends AppSettingsTile {

    public AppSettingsSwitchTile(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(getSwitchState());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                applySwitchState(checked);
            }
        };
    }

    boolean getSwitchState() {
        return false;
    }

    void applySwitchState(boolean checked) {

    }
}
