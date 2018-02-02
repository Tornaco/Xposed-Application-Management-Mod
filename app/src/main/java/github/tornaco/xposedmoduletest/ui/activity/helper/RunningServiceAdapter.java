package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.app.PerAppSettingsDashboardActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;

/**
 * Created by guohao4 on 2017/12/20.
 * Email: Tornaco@163.com
 */

public class RunningServiceAdapter extends CommonPackageInfoAdapter {

    RunningServiceAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getTemplateLayoutRes() {
        return R.layout.app_list_item_2;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final RunningServiceInfoDisplay display = (RunningServiceInfoDisplay) getCommonPackageInfos().get(position);
        holder.getLineTwoTextView().setText(display.getMergedItem().mDescription);
        holder.getSystemAppIndicator().setVisibility(View.VISIBLE);
        if (BuildConfig.DEBUG) {
            Logger.e("mem for this app: " + display.getMergedItem().mSizeStr);
        }
        holder.getSystemAppIndicator().setText(display.getMergedItem().mSizeStr);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!display.isSystemApp()) {
                    PerAppSettingsDashboardActivity.start(getContext(), display.getPkgName());
                } else {
                    Toast.makeText(getContext(), R.string.system_app_not_allowed_set_running_services,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected int getSystemAppIndicatorColor(int appLevel) {
        return ContextCompat.getColor(getContext(), R.color.amber_dark);
    }

    @Override
    protected boolean onItemLongClick(View v, int position) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
