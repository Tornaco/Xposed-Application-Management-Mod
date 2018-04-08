package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */

public class RunningServicesActivity
        extends CommonPackageInfoListActivity {

    private int mClearedPackageNum = 0;

    @Override
    protected void initView() {
        super.initView();
        fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
    }

    @Override
    protected void onFabClick() {
        super.onFabClick();
        if (XAshmanManager.get().isServiceAvailable()) {
            XAshmanManager.get().clearProcess(
                    new IProcessClearListenerAdapter() {
                        @Override
                        public void onPrepareClearing() throws RemoteException {
                            super.onPrepareClearing();
                            mClearedPackageNum = 0;
                        }

                        @Override
                        public void onClearedPkg(String pkg) throws RemoteException {
                            super.onClearedPkg(pkg);
                            mClearedPackageNum++;
                        }

                        @Override
                        public void onAllCleared(final String[] pkg) throws RemoteException {
                            super.onAllCleared(pkg);
                            XExecutor.getUIThreadHandler()
                                    .post(() -> Toast.makeText(getApplicationContext(),
                                            R.string.clear_process_complete, Toast.LENGTH_LONG).show());

                            if (!isDestroyed()) {
                                runOnUiThreadChecked(() -> startLoading());
                            }
                        }

                        @Override
                        public boolean doNotClearWhenIntervative() throws RemoteException {
                            return false;
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // mState.resume();
    }

    @Override
    protected int getSummaryRes() {
        return 0;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new RunningServiceAdapter(this);
    }

    private boolean mShowCache;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.running_services, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_cached) {
            mShowCache = !mShowCache;
            startLoading();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        if (isDestroyed()) return new ArrayList<>();
        RunningServicesLoadingCache.getInstance().refresh();
        List<RunningState.MergedItem> items = RunningServicesLoadingCache.getInstance()
                .getRunningServiceCache().getList();
        ArrayList<RunningServiceInfoDisplay> displays = new ArrayList<>();
        for (RunningState.MergedItem m : items) {
            RunningServiceInfoDisplay d = new RunningServiceInfoDisplay(this, m);
            if (BuildConfig.APPLICATION_ID.equals(d.getPkgName())) continue;
            d.setSystemApp(PkgUtil.isSystemApp(getApplicationContext(), d.getPkgName()));
            displays.add(d);
            Logger.w("RunningServiceInfoDisplay-LAZY:" + m.mLabel);
            Logger.w("RunningServiceInfoDisplay-LAZY:" + m.mServices);
        }
        return displays;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
