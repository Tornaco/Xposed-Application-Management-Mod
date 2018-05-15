package github.tornaco.xposedmoduletest.ui.activity.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyAndroidId;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyApps;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyDeviceId;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyIccSerial;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

@RuntimePermissions
public class PrivacyNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, PrivacyNavActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list_priv;
    }

    @Override
    protected void onRequestClearItemsInBackground() {

    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAPMManager.get().isPrivacyEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        fab = findViewById(R.id.fab);
        fab.hide();

        PrivacyNavActivityPermissionRequester.setupViewsChecked(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDash != null) {
            mDash.reload();
        }
    }

    private Dashboards mDash;

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresPermission.OnDenied("onPermissionDenied")
    void setupViews() {
        mDash = new Dashboards();
        replaceV4(R.id.container, mDash, null, false);
    }

    void onPermissionDenied() {
        Toast.makeText(getActivity(), R.string.title_privacy_perm_denied, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PrivacyNavActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected boolean hasRecyclerView() {
        return false;
    }

    @Override
    protected void onRequestPick() {

    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_privacy;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this){
            @Override
            protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
                super.onItemClickNoneChoiceMode(commonPackageInfo, view);
                showCommonItemPopMenu(commonPackageInfo, view);
            }
        };
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return new ArrayList<>(0);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAPMManager.get().setPrivacyEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.privacy, menu);
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

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        public void reload() {
            buildUI(getActivity());
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category list = new Category();
            list.titleRes = R.string.title_apps;
            list.addTile(new PrivacyApps(getActivity()));

            Category items = new Category();
            items.titleRes = R.string.title_privacy_items;
            items.addTile(new PrivacyAndroidId(getContext()));
            items.addTile(new PrivacyDeviceId(getContext()));
            items.addTile(new PrivacyIccSerial(getContext()));

            categories.add(list);
            categories.add(items);
        }
    }
}