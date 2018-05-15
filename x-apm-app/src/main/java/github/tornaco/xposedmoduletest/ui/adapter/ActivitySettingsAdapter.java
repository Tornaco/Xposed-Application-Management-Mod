package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ActivitySettingsAdapter extends ComponentListAdapter<ActivityInfoSettings>
        implements FastScrollRecyclerView.SectionedAdapter {

    public ActivitySettingsAdapter(Context context) {
        super(context);
        xAshmanManager = XAPMManager.get();
    }

    private XAPMManager xAshmanManager;

    @Override
    int getTemplateLayoutRes() {
        return R.layout.comp_list_item_activity;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final ActivityInfoSettings activityInfoSettings = getData().get(position);

        holder.getTitleView().setText(activityInfoSettings.simpleName());

        String name = activityInfoSettings.getActivityInfo().name;

        holder.getSummaryView().setText(getContext().getString(R.string.summary_service_info_comp, name));
        holder.getSummaryView2().setText(getContext().getString(R.string.summary_service_info_comp_maybe_ad));
        holder.getSummaryView2().setVisibility(View.GONE);

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

                switch (getMode()) {
                    case COMPONENT_ENABLE_DISABLE:
                        xAshmanManager.setComponentEnabledSetting(componentName,
                                checked ?
                                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        break;
                    case IFW:

                        break;
                }

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

    @Override
    protected PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener(final ActivityInfoSettings activityInfoSettings) {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_copy_name) {
                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cmb != null) {
                        String content = ComponentUtil.getComponentName(activityInfoSettings.getActivityInfo()).flattenToString();
                        Logger.w("content: " + content);
                        cmb.setPrimaryClip(ClipData.newPlainText("comp_name", content));
                    }
                    return true;
                }
                return false;
            }
        };
    }
}
