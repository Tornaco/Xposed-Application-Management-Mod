package github.tornaco.xposedmoduletest.ui.activity.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

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
                    IAshmanService service = IAshmanService.Stub.asInterface(ServiceManager.getService(XAshmanManager.SERVICE_NAME));
                    try {
                        service.createMultipleProfile();
                    } catch (RemoteException ignored) {

                    }
                });

        findViewById(R.id.test_btn_install_apm)
                .setOnClickListener(v -> {
                    IAshmanService service = IAshmanService.Stub.asInterface(ServiceManager.getService(XAshmanManager.SERVICE_NAME));
                    try {
                        service.installAppToMultipleAppsUser(BuildConfig.APPLICATION_ID);
                    } catch (RemoteException ignored) {

                    }
                });

        findViewById(R.id.test_btn_launch_apm)
                .setOnClickListener(v -> {
                    IAshmanService service =
                            IAshmanService.Stub.asInterface(ServiceManager.getService(XAshmanManager.SERVICE_NAME));
                    PackageManager pm = getPackageManager();
                    Intent launcher = pm.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                    try {
                        service.startActivityAsUser(launcher, 10);
                    } catch (RemoteException ignored) {
                        org.newstand.logger.Logger.e("MultipleAppsManager: " + Log.getStackTraceString(ignored));
                    }
                });
    }
}
