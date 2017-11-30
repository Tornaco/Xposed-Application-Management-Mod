package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.tornaco.vangogh.display.ImageEffect;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardAppPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/18.
 * Email: Tornaco@163.com
 */

public class PackageViewerActivity extends GuardAppPickerActivity {

    private String disabledString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disabledString = getString(R.string.title_package_disabled);
    }

    @Override
    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppListAdapter(this) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(final AppViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);

                // Block all when xash is not running.
                if (!XAshmanManager.singleInstance().isServiceAvailable()) return;

                final PackageInfo packageInfo = getPackageInfos().get(position);
                final boolean disabled = packageInfo.isDisabled();
                if (disabled) {
                    holder.getLineOneTextView().setTextColor(Color.RED);
                    holder.getLineOneTextView().setText(packageInfo.getAppName() + disabledString);
                } else {
                    holder.getLineOneTextView().setTextColor(Color.BLACK);
                }
                holder.itemView.setOnLongClickListener(null);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopMenu(packageInfo, disabled, v);
                    }
                });
            }
        };
    }

    private void showPopMenu(final PackageInfo packageInfo, boolean isDisabledCurrently, View anchor) {
        PopupMenu popupMenu = new PopupMenu(PackageViewerActivity.this, anchor);
        popupMenu.inflate(R.menu.package_viewer);
        if (isDisabledCurrently) {
            popupMenu.getMenu().findItem(R.id.action_enable_app).setVisible(true);
        } else {
            popupMenu.getMenu().findItem(R.id.action_disable_app).setVisible(true);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_disable_app:
                        XAshmanManager.singleInstance().setApplicationEnabledSetting(
                                packageInfo.getPkgName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        startLoading();
                        break;
                    case R.id.action_enable_app:
                        XAshmanManager.singleInstance().setApplicationEnabledSetting(
                                packageInfo.getPkgName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                        startLoading();
                        break;
                    case R.id.action_comp_edit:
                        ComponentEditorActivity.start(getActivity(), packageInfo.getPkgName());
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void setSummaryView() {
        super.setSummaryView();
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.VISIBLE);
        textView.setText(R.string.summary_comp_edit);
    }

    @Override
    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
    }
}
