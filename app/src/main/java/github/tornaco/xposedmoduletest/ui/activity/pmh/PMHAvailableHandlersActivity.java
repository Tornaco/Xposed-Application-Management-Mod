package github.tornaco.xposedmoduletest.ui.activity.pmh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.TGPMH;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.TGPMHShowContentSettings;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMH;
import github.tornaco.xposedmoduletest.ui.tiles.pmh.WeChatPMHShowContentSettings;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.TGPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class PMHAvailableHandlersActivity extends BaseActivity {

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

    public static class Dashboards extends AppCustomDashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            // WeChat.
            Category wechat = new Category();
            if (PkgUtil.isPkgInstalled(getActivity(), WeChatPushNotificationHandler.WECHAT_PKG_NAME)) {
                wechat.addTile(new WeChatPMH(getActivity()));
                wechat.addTile(new WeChatPMHShowContentSettings(getActivity()));
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
