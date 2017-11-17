package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.widget.CompoundButton;

import github.tornaco.xposedmoduletest.bean.ComponentSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.provider.ComponentsProvider;
import github.tornaco.xposedmoduletest.util.ComponentUtil;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ServiceSettingsAdapter extends ComponentListAdapter<ServiceInfoSettings> {

    public ServiceSettingsAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final ServiceInfoSettings serviceInfoSettings = getData().get(position);
        holder.getTitleView().setText(serviceInfoSettings.getServiceInfo().name);
        holder.getSummaryView().setText(serviceInfoSettings.getServiceInfo().toString());
        holder.getCompSwitch().setChecked(serviceInfoSettings.isAllowed());

        holder.getCompSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                serviceInfoSettings.setAllowed(!serviceInfoSettings.isAllowed());
                ComponentSettings componentSettings = new ComponentSettings();
                componentSettings.setAllow(serviceInfoSettings.isAllowed());
                ComponentName componentName = ComponentUtil.getComponentName(serviceInfoSettings.getServiceInfo());
                componentSettings.setClassName(componentName.getClassName());
                componentSettings.setPackageName(componentName.getPackageName());
                ComponentsProvider.insertOrUpdate(getContext(), componentSettings);
            }
        });
    }
}
