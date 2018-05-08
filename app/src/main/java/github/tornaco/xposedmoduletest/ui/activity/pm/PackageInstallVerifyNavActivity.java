package github.tornaco.xposedmoduletest.ui.activity.pm;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.PackageInstallVerifyRuleLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class PackageInstallVerifyNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, PackageInstallVerifyNavActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        XAshmanManager.get().addOrRemovePackageInstallerVerifyRules(commonPackageInfo.getAppName(), false);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAshmanManager.get().isPackageInstallVerifyEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setupViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDash != null) {
            mDash.reload();
        }
    }

    private Dashboards mDash;

    void setupViews() {
        mDash = new Dashboards();
        replaceV4(R.id.container, mDash, null, false);
    }

    @Override
    protected void onRequestPick() {
        final EditText e = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_create_new_rule)
                .setView(e)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            String text = e.getText().toString();
                            boolean added = XAshmanManager.get().addOrRemovePackageInstallerVerifyRules(text, true);
                            if (added) {
                                startLoading();
                            } else {
                                Toast.makeText(getContext(), R.string.err_rule_add_fail, Toast.LENGTH_SHORT).show();
                            }
                        })
                .show();
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_package_install_verify;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return PackageInstallVerifyRuleLoader.Impl.create(this).loadInstalled();
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAshmanManager.get().setPackageInstallVerifyEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.package_install_verify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void reload() {
        getUIThreadHandler().postDelayed(() -> {
            if (!isDestroyed()) {
                mDash.reload();
            }
        }, 500);
    }

    public static class Dashboards extends AppCustomDashboardFragment {

        public void reload() {
            buildUI(getActivity());
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
        }
    }
}