package github.tornaco.xposedmoduletest.ui.activity.doze;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.DozeWhitelistPackageLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/1/19.
 * Email: Tornaco@163.com
 */

public class DozeWhiteListViewerActivity extends CommonPackageInfoListActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, DozeWhiteListViewerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Logger.e("onRequestClearItemsInBackground");
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAPMManager.get().removePowerSaveWhitelistApp(commonPackageInfo.getPkgName());
                        }
                    }
                });
    }

    @Override
    protected void onRequestPick() {
        DozeWhiteListPickerActivity.start(getActivity());
        Logger.e("onRequestPick@Doze");
    }

    @Override
    protected int getSummaryRes() {
        return 0;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this) {
            @Override
            public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                boolean isSystem = packageInfo.getPayload() != null;
                if (isSystem) {
                    holder.getThirdTextView().setText(R.string.title_doze_system_app);
                }
            }
        };
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        return DozeWhitelistPackageLoader.Impl.create(this).loadWhiteList(true);
    }
}
