package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.xposed.XAppBuildHostInfo;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class AppVersion extends QuickTile {
    private int clickedTimes = 0;

    public AppVersion(final Context context) {
        super(context);

        this.titleRes = R.string.title_app_ver;
        this.summary = BuildConfig.VERSION_NAME + "-" + BuildConfig.BUILD_TYPE.toUpperCase()
                + "\n编译主机：" + XAppBuildHostInfo.BUILD_HOST_NAME
                + "\n编译日期：" + XAppBuildHostInfo.BUILD_DATE;
//                + "\n提交：" + XAppGithubCommitSha.LATEST_SHA
//                + "\nServer序列号：" + (XAshmanManager.get().isServiceAvailable() ? XAshmanManager.get().getBuildSerial() : "UNKNOWN")
//                + "\nApp序列号：" + BuildFingerprintBuildHostInfo.BUILD_FINGER_PRINT;
        this.iconRes = R.drawable.ic_info_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                clickedTimes++;

                if (clickedTimes >= 8) {
                    if (!AppSettings.isShowInfoEnabled(context, "show_lazy_ui", false)) {
                        AppSettings.setShowInfo(context, "show_lazy_ui", true);
                        Toast.makeText(context, "@$#%@&#%@^#%^#", Toast.LENGTH_SHORT).show();
                    }
                    clickedTimes = 0;
                }
            }
        };
    }
}
