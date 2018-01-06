package github.tornaco.xposedmoduletest.ui.activity.doze;

import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DozeDelayTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.DozeEnterTile;
import github.tornaco.xposedmoduletest.ui.tiles.doze.ForceDozeTile;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class DozeNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

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
        switchBar.setChecked(XAshmanManager.get().isDozeEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        fab = findViewById(R.id.fab);
        fab.hide();

        replaceV4(R.id.container, new Dashboards(), null, false);
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
        XAshmanManager.get().setDozeEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static class Dashboards extends DashboardFragment {
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

            categories.add(state);
            categories.add(config);
        }
    }
}