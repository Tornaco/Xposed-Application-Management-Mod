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
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ServiceSettingsAdapter extends ComponentListAdapter<ServiceInfoSettings>
        implements FastScrollRecyclerView.SectionedAdapter {

    public ServiceSettingsAdapter(Context context) {
        super(context);
    }

    private final XAPMManager xAshmanManager = XAPMManager.get();

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final ServiceInfoSettings serviceInfoSettings = getData().get(position);

        holder.getTitleView().setText(serviceInfoSettings.simpleName());

        String name = serviceInfoSettings.getServiceInfo().name;

        holder.getSummaryView().setText(getContext().getString(R.string.summary_service_info_comp, name));

        holder.getSummaryView2().setText(getContext().getString(R.string.summary_service_info_comp_maybe_ad));
        holder.getSummaryView2().setVisibility(serviceInfoSettings.mayBeAdComponent() ? View.VISIBLE : View.GONE);

        if (!xAshmanManager.isServiceAvailable()) {
            return;
        }

        holder.getCompSwitch().setChecked(serviceInfoSettings.isAllowed());

        holder.getCompSwitch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName componentName = ComponentUtil.getComponentName(serviceInfoSettings.getServiceInfo());
                Switch s = (Switch) v;
                boolean checked = s.isChecked();

                serviceInfoSettings.setAllowed(checked);

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
        ServiceInfoSettings infoSettings = getData().get(position);
        String name = infoSettings.simpleName();
        if (name == null || name.length() < 1) {
            name = infoSettings.toString();
        }
        return String.valueOf(name.charAt(0));
    }

    @Override
    protected int getPopupMenuRes() {
        return R.menu.component_service_item;
    }

    @Override
    protected PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener(final ServiceInfoSettings settings) {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_copy_name) {
                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cmb != null) {
                        cmb.setPrimaryClip(ClipData.newPlainText("comp_name",
                                ComponentUtil.getComponentName(settings.getServiceInfo()).flattenToString()));
                    }
                    return true;
                }

                if (item.getItemId() == R.id.action_make_lazy_rule_keep) {
                    ComponentName componentName = ComponentUtil.getComponentName(settings.getServiceInfo());
                    String rule = String.format("KEEP %s %s", componentName.getPackageName(), componentName.flattenToShortString());
                    XAPMManager.get().addOrRemoveLazyRules(rule, true);
                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cmb != null) {
                        cmb.setPrimaryClip(ClipData.newPlainText("lazy_keep_rule", rule));
                    }
                    Toast.makeText(getContext(), R.string.title_make_lazy_rule_as_keep_success, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        };
    }
}
