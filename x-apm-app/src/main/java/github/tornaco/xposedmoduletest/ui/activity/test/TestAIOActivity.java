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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.io.Closer;

import org.newstand.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dev.nick.tiles.tile.Tile;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.TileManager;
import github.tornaco.xposedmoduletest.util.XExecutor;
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
                        service.installAppToMultipleAppsUser("com.tencent.mm");
                    } catch (RemoteException ignored) {

                    }
                });

        findViewById(R.id.test_btn_launch_apm)
                .setOnClickListener(v -> {
                    IAshmanService service =
                            IAshmanService.Stub.asInterface(ServiceManager.getService(XAPMManager.SERVICE_NAME));
                    PackageManager pm = getPackageManager();
                    Intent launcher = pm.getLaunchIntentForPackage("com.tencent.mm");
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

        findViewById(R.id.test_btn_screenshot)
                .setOnClickListener(v -> {
                    XAPMManager.get().takeLongScreenShot();
                });


        findViewById(R.id.test_btn_js)
                .setOnClickListener(v -> {

                    Closer closer = Closer.create();
                    try {
                        InputStream is = getAssets().open("js/demo.js");
                        StringBuilder rawScript = new StringBuilder();
                        BufferedReader br = closer.register(new BufferedReader(new InputStreamReader(is)));
                        String line;
                        while ((line = br.readLine()) != null) {
                            rawScript.append(line).append("\n");
                        }

                        Logger.d(rawScript);

                        final EditText e = new EditText(getActivity());
                        e.setText(rawScript.toString());
                        new AlertDialog.Builder(getActivity())
                                .setTitle("JS")
                                .setView(e)
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    String text = e.getText().toString();
                                    XExecutor.execute(() -> XAPMManager.get().evaluateJsString(new String[]{text}));
                                })
                                .show();


                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        github.tornaco.xposedmoduletest.xposed.util.Closer.closeQuietly(closer);
                    }
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
        } finally {
            TabTestActivity.start(getActivity());
        }
    }
}
