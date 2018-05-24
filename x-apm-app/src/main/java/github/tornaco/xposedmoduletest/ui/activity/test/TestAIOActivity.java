package github.tornaco.xposedmoduletest.ui.activity.test;

import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.Tile;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.TileManager;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */

public class TestAIOActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, TestAIOActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleRes();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_aio_only_dev);

        findViewById(R.id.test_btn_create_ma_user)
                .setOnClickListener(v -> {
                    IAshmanService service = IAshmanService.Stub.asInterface(ServiceManager.getService(XAPMManager.SERVICE_NAME));
                    try {
                        service.createMultipleProfile();
                    } catch (RemoteException ignored) {

                    }
                });

        findViewById(R.id.test_btn_install_apm)
                .setOnClickListener(v -> {
                    IAshmanService service = IAshmanService.Stub.asInterface(ServiceManager.getService(XAPMManager.SERVICE_NAME));
                    try {
                        service.installAppToMultipleAppsUser(BuildConfig.APPLICATION_ID);
                    } catch (RemoteException ignored) {

                    }
                });

        findViewById(R.id.test_btn_launch_apm)
                .setOnClickListener(v -> {
                    IAshmanService service =
                            IAshmanService.Stub.asInterface(ServiceManager.getService(XAPMManager.SERVICE_NAME));
                    PackageManager pm = getPackageManager();
                    Intent launcher = pm.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                    try {
                        service.startActivityAsUser(launcher, 10);
                    } catch (RemoteException ignored) {
                        org.newstand.logger.Logger.e("MultipleAppsManager: " + Log.getStackTraceString(ignored));
                    }
                });

        findViewById(R.id.test_btn_input)
                .setOnClickListener(v -> {
                    XAPMManager.get().executeInputCommand(new String[]{"-h"});

                    XAPMManager.get().executeInputCommand(new String[]{
                            "swipe",
                            "300",
                            "300",
                            "900",
                            "900"
                    });
                });

        Tile tile = TileManager.makeTileByKey("Lazy", getActivity());
        Logger.d("makeTileByKey: " + tile);

        AppSettings.getRecentTiles(this);

        try {
            IUsageStatsManager.Stub.asInterface(ServiceManager
                    .getService(Context.USAGE_STATS_SERVICE))
                    .setAppInactive(BuildConfig.APPLICATION_ID, true, UserHandle.getCallingUserId());
        } catch (Throwable e) {
            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
