package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Switch;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.ReceiverInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ReceiverSettingsAdapter extends ComponentListAdapter<ReceiverInfoSettings> {

    public ReceiverSettingsAdapter(Context context) {
        super(context);
        xAshmanManager = XAshmanManager.singleInstance();
    }

    private XAshmanManager xAshmanManager;

    @Override
    int getTemplateLayoutRes() {
        return R.layout.comp_list_item_receiver;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final ReceiverInfoSettings receiverInfoSettings = getData().get(position);


        holder.getTitleView().setText(receiverInfoSettings.getDisplayName());

        String processName = receiverInfoSettings.getActivityInfo().processName;
        String serviceLabel = receiverInfoSettings.getServiceLabel();


        holder.getSummaryView().setText(getContext().getString(R.string.summary_service_info_process,
                processName));
        holder.getSummaryView2().setText(getContext().getString(R.string.summary_service_info_comp,
                serviceLabel));

        if (!xAshmanManager.isServiceAvailable()) {
            return;
        }

        holder.getCompSwitch().setChecked(receiverInfoSettings.isAllowed());

        holder.getCompSwitch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                boolean checked = s.isChecked();
                Logger.d("checked: " + checked);
                receiverInfoSettings.setAllowed(checked);
                ComponentName componentName = ComponentUtil.getComponentName(receiverInfoSettings.getActivityInfo());
                xAshmanManager.setComponentEnabledSetting(componentName,
                        checked ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            }
        });
    }
}
