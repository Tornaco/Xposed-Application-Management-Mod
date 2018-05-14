package github.tornaco.xposedmoduletest.ui.activity.common;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public abstract class CommonPackageInfoListPickerActivity
        extends CommonPackageInfoListActivity {

    private boolean selectAll = false;
    private String mRawTitle;

    @Override
    protected void initView() {
        super.initView();
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        mRawTitle = String.valueOf(getTitle());
    }

    @Override
    protected void startLoading() {
        super.startLoading();
        setTitle(mRawTitle);
    }

    @Override
    public void onItemCheckChanged(int total, int checked) {
        super.onItemCheckChanged(total, checked);
        String tips = "\t" + checked + "/" + total;
        setTitle(mRawTitle + tips);
    }

    @Override
    protected void onFabClick() {
        onRequestPick();
    }

    @Override
    protected void onRequestPick() {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.setCancelable(false);
        p.setIndeterminate(true);
        p.setMessage(getString(R.string.message_saving_changes));
        p.show();

        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isDestroyed()) return;
                    doOnFabClickInWorkThread();
                } catch (final Throwable e) {
                    Logger.e("doOnFabClickInWorkThread: " + Logger.getStackTraceString(e));
                    if (isDestroyed()) return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSimpleDialog(getString(R.string.title_error_occur), Logger.getStackTraceString(e));
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            p.dismiss();
                            finish();
                        }
                    });
                }

            }
        });
    }

    protected abstract void doOnFabClickInWorkThread();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select_all) {
            selectAll = !selectAll;
            getCommonPackageInfoAdapter().selectAll(selectAll);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected int getSummaryRes() {
        return 0;
    }
}
