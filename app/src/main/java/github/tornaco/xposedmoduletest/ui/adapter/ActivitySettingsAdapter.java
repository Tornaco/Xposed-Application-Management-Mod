package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Switch;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ActivitySettingsAdapter extends ComponentListAdapter<ActivityInfoSettings>
        implements FastScrollRecyclerView.SectionedAdapter {

    public ActivitySettingsAdapter(Context context) {
        super(context);
        xAshmanManager = XAshmanManager.singleInstance();
    }

    private XAshmanManager xAshmanManager;

    @Override
    int getTemplateLayoutRes() {
        return R.layout.comp_list_item_activity;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final ActivityInfoSettings activityInfoSettings = getData().get(position);

        holder.getTitleView().setText(activityInfoSettings.simpleName());

        String processName = activityInfoSettings.getActivityInfo().processName;
        String serviceLabel = activityInfoSettings.getServiceLabel();

        holder.getSummaryView().setText(getContext().getString(R.string.summary_service_info_process,
                processName));
        holder.getSummaryView2().setText(getContext().getString(R.string.summary_service_info_comp,
                serviceLabel));

        if (!xAshmanManager.isServiceAvailable()) {
            return;
        }

        holder.getCompSwitch().setChecked(activityInfoSettings.isAllowed());

        holder.getCompSwitch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                boolean checked = s.isChecked();
                activityInfoSettings.setAllowed(checked);
                ComponentName componentName = ComponentUtil.getComponentName(activityInfoSettings.getActivityInfo());
                xAshmanManager.setComponentEnabledSetting(componentName,
                        checked ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            }
        });
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        ActivityInfoSettings activityInfo = getData().get(position);
        String name = activityInfo.simpleName();
        if (name == null || name.length() < 1) {
            name = activityInfo.toString();
        }
        return String.valueOf(name.charAt(0));
    }
}
