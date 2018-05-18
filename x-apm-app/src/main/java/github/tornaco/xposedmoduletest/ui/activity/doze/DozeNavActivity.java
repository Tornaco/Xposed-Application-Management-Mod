package github.tornaco.xposedmoduletest.ui.activity.doze;

import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DisableMotionTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DozeDelayTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DozeEnterTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DozeWhitelistTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.ForceDozeTile;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class DozeNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    private Dashboards mDash;

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list_doze;
    }

    @Override
    protected void onRequestClearItemsInBackground() {

    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAPMManager.get().isDozeEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        fab = findViewById(R.id.fab);
        fab.hide();

        mDash = new Dashboards();
        replaceV4(R.id.container, mDash, null, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDash.onResume();
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
        return R.string.summary_doze;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return new ArrayList<>(0);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAPMManager.get().setDozeEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.doze, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

            Category state = new Category();
            state.titleRes = R.string.title_state;
            state.addTile(new DozeEnterTile(getActivity()));

            Category config = new Category();
            config.titleRes = R.string.title_config;
            config.addTile(new DozeDelayTile(getActivity()));
            config.addTile(new ForceDozeTile(getActivity()));
            config.addTile(new DisableMotionTile(getActivity()));
            config.addTile(new DozeWhitelistTile(getActivity()));

            categories.add(state);
            categories.add(config);
        }
    }
}