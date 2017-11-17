package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.widget.CompoundButton;

import github.tornaco.xposedmoduletest.bean.ComponentSettings;
import github.tornaco.xposedmoduletest.model.ReceiverInfoSettings;
import github.tornaco.xposedmoduletest.provider.ComponentsProvider;
import github.tornaco.xposedmoduletest.util.ComponentUtil;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ReceiverSettingsAdapter extends ComponentListAdapter<ReceiverInfoSettings> {

    public ReceiverSettingsAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ReceiverInfoSettings receiverInfoSettings = getData().get(position);
        holder.getTitleView().setText(receiverInfoSettings.getActivityInfo().name);
        holder.getSummaryView().setText(receiverInfoSettings.getActivityInfo().toString());
        holder.getCompSwitch().setChecked(receiverInfoSettings.isAllowed());
        holder.getCompSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                receiverInfoSettings.setAllowed(!receiverInfoSettings.isAllowed());
                ComponentSettings componentSettings = new ComponentSettings();
                componentSettings.setAllow(receiverInfoSettings.isAllowed());
                ComponentName componentName = ComponentUtil.getComponentName(receiverInfoSettings.getActivityInfo());
                componentSettings.setClassName(componentName.getClassName());
                componentSettings.setPackageName(componentName.getPackageName());
                ComponentsProvider.insertOrUpdate(getContext(), componentSettings);
            }
        });
    }
}
