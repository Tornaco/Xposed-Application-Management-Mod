package github.tornaco.xposedmoduletest.ui.activity.green2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.ag.GuardAppPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.SpannableUtil;
import github.tornaco.xposedmoduletest.xposed.XApp;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/12/16.
 * Email: Tornaco@163.com
 */

public class ExtraOpsSettingActivity extends GuardAppPickerActivity {

    @Override
    protected void initView() {
        super.initView();

        findViewById(R.id.fab).setVisibility(View.GONE);

        SwitchBar switchBar = findViewById(R.id.switchbar);
        switchBar.hide();
    }

    @Override
    protected void setSummaryView() {
        String who = getClass().getSimpleName();
        boolean showInfo = AppSettings.isShowInfoEnabled(this, who);
        TextView textView = findViewById(R.id.summary);
        if (!showInfo) {
            textView.setVisibility(View.GONE);
        } else {
            int normalColor = ContextCompat.getColor(getActivity(), R.color.white);
            int highlightColor = ContextCompat.getColor(getActivity(), R.color.amber);
            int strId = R.string.summary_greening_app;
            textView.setText(SpannableUtil.buildHighLightString(getActivity(), normalColor, highlightColor, strId));
            textView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected List<PackageInfo> performLoading() {
        List<PackageInfo> res = PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
        if (Collections.isNullOrEmpty(res)) return res;

        final List<PackageInfo> checked = new ArrayList<>();

        Collections.consumeRemaining(res, new Consumer<PackageInfo>() {
            @Override
            public void accept(PackageInfo packageInfo) {
                if (XApp.isInGlobalWhiteList(packageInfo.getPkgName())) return;

                int modeService = XAshmanManager.get()
                        .getPermissionControlBlockModeForPkg(
                                AppOpsManagerCompat.OP_START_SERVICE, packageInfo.getPkgName());
                int modeWakelock = XAshmanManager.get()
                        .getPermissionControlBlockModeForPkg(
                                AppOpsManagerCompat.OP_WAKE_LOCK, packageInfo.getPkgName());
                int modeAlarm = XAshmanManager.get()
                        .getPermissionControlBlockModeForPkg(
                                AppOpsManagerCompat.OP_SET_ALARM, packageInfo.getPkgName());
                packageInfo.setService(modeService == AppOpsManagerCompat.MODE_ALLOWED);
                packageInfo.setAlarm(modeAlarm == AppOpsManagerCompat.MODE_ALLOWED);
                packageInfo.setWakeLock(modeWakelock == AppOpsManagerCompat.MODE_ALLOWED);

                checked.add(packageInfo);
            }
        });

        java.util.Collections.sort(checked, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                if (o1.getDistance() > o2.getDistance()) return 1;
                return -1;
            }
        });


        return checked;
    }

    @Override
    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppListAdapter(this) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(final AppViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);

                final PackageInfo packageInfo = getPackageInfos().get(position);

                String summary = packageInfo.isAllExtraPermDisabled() ? "全部禁止" : "";
                if (packageInfo.isService()) {
                    summary += "服务\t\t";
                }
                if (packageInfo.isWakeLock()) {
                    summary += "唤醒锁\t\t";
                }
                if (packageInfo.isAlarm()) {
                    summary += "唤醒定时器";
                }
                holder.getLineTwoTextView().setText(summary);

                holder.itemView.setOnLongClickListener(null);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showExtraPermSettinDialog(packageInfo);
                    }
                });
            }

            @Override
            protected int getTemplateLayoutRes() {
                return R.layout.app_list_item_2;
            }
        };
    }


    private void showExtraPermSettinDialog(final PackageInfo packageInfo) {
        int modeService = XAshmanManager.get()
                .getPermissionControlBlockModeForPkg(
                        AppOpsManagerCompat.OP_START_SERVICE, packageInfo.getPkgName());
        int modeWakelock = XAshmanManager.get()
                .getPermissionControlBlockModeForPkg(
                        AppOpsManagerCompat.OP_WAKE_LOCK, packageInfo.getPkgName());
        int modeAlarm = XAshmanManager.get()
                .getPermissionControlBlockModeForPkg(
                        AppOpsManagerCompat.OP_SET_ALARM, packageInfo.getPkgName());


        final String[] items = {"服务", "唤醒锁", "唤醒定时器"};
        new AlertDialog.Builder(getActivity()).setCancelable(false)
                .setTitle("自定义")
                .setMultiChoiceItems(items, new boolean[]{
                                modeService == AppOpsManagerCompat.MODE_ALLOWED,
                                modeWakelock == AppOpsManagerCompat.MODE_ALLOWED,
                                modeAlarm == AppOpsManagerCompat.MODE_ALLOWED},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                int op = -1;
                                if (which == 0) op = AppOpsManagerCompat.OP_START_SERVICE;
                                if (which == 1) op = AppOpsManagerCompat.OP_WAKE_LOCK;
                                if (which == 2) op = AppOpsManagerCompat.OP_SET_ALARM;
                                int mode = isChecked ? AppOpsManagerCompat.MODE_ALLOWED : AppOpsManagerCompat.MODE_IGNORED;
                                XAshmanManager.get().setPermissionControlBlockModeForPkg(op, packageInfo.getPkgName(), mode);
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                dialoginterface.dismiss();

                                // Retrieve new state.
                                int modeService = XAshmanManager.get()
                                        .getPermissionControlBlockModeForPkg(
                                                AppOpsManagerCompat.OP_START_SERVICE, packageInfo.getPkgName());
                                int modeWakelock = XAshmanManager.get()
                                        .getPermissionControlBlockModeForPkg(
                                                AppOpsManagerCompat.OP_WAKE_LOCK, packageInfo.getPkgName());
                                int modeAlarm = XAshmanManager.get()
                                        .getPermissionControlBlockModeForPkg(
                                                AppOpsManagerCompat.OP_SET_ALARM, packageInfo.getPkgName());

                                packageInfo.setService(modeService == AppOpsManagerCompat.MODE_ALLOWED);
                                packageInfo.setAlarm(modeAlarm == AppOpsManagerCompat.MODE_ALLOWED);
                                packageInfo.setWakeLock(modeWakelock == AppOpsManagerCompat.MODE_ALLOWED);

                                getGuardAppListAdapter().notifyDataSetChanged();
                            }
                        })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.perm_viewer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.show_system_app).setChecked(mShowSystemApp);
        menu.findItem(R.id.action_info).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
