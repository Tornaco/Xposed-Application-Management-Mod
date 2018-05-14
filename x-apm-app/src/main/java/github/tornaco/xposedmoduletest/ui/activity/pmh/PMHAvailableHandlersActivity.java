package github.tornaco.xposedmoduletest.ui.activity.pmh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.TGPMH;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.TGPMHShowContentSettings;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMH;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHMockMessage;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHNotificationPostByApp;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHNotificationSettingsOreo;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHShowContentSettings;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHSoundSettings;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHVibrateSettings;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.TGPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class PMHAvailableHandlersActivity extends WithWithCustomTabActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, PMHAvailableHandlersActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.available_pmh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.action_help){
            // WTF? like a shit...
            String url = "https://github.com/Tornaco/X-APM/wiki/GCM%E6%B6%88%E6%81%AF%E4%BB%A3%E6%94%B6#%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF%E9%80%9A%E7%9F%A5%E8%A1%8C%E4%B8%BA";
            navigateToWebPage(url);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class Dashboards extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            // WeChat.
            Category wechat = new Category();
            if (PkgUtil.isPkgInstalled(getActivity(), WeChatPushNotificationHandler.WECHAT_PKG_NAME)) {
                wechat.addTile(new WeChatPMH(getActivity()));
                wechat.addTile(new WeChatPMHShowContentSettings(getActivity()));
                wechat.addTile(new WeChatPMHNotificationPostByApp(getActivity()));
                if (OSUtil.isOOrAbove()) {
                    wechat.addTile(new WeChatPMHNotificationSettingsOreo(getActivity()));
                } else {
                    wechat.addTile(new WeChatPMHSoundSettings(getActivity()));
                    wechat.addTile(new WeChatPMHVibrateSettings(getActivity()));
                }
                wechat.addTile(new WeChatPMHMockMessage(getActivity()));
            }

            // TG.
            Category tg = new Category();
            if (PkgUtil.isPkgInstalled(getActivity(), TGPushNotificationHandler.TG_PKG_NAME)) {
                tg.addTile(new TGPMH(getActivity()));
                tg.addTile(new TGPMHShowContentSettings(getActivity()));
            }

            if (wechat.getTilesCount() > 0) categories.add(wechat);
            if (tg.getTilesCount() > 0) categories.add(tg);
        }
    }


}
