package github.tornaco.xposedmoduletest.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import github.tornaco.xposedmoduletest.R;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/9/21.
 * Email: Tornaco@163.com
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements View {

    private Handler uiThreadHandler;

    @Synchronized
    // Lazy.
    public Handler getUIThreadHandler() {
        if (uiThreadHandler == null) uiThreadHandler = new Handler(Looper.getMainLooper());
        return uiThreadHandler;
    }

    protected void setupToolbar() {
        setupToolbar(R.id.toolbar);
    }

    protected void setupToolbar(int resId) {
        Toolbar toolbar = (Toolbar) findViewById(resId);
        setSupportActionBar(toolbar);
    }

    @Override
    public void showHomeAsUp() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void showTips(CharSequence tips, boolean infinite, String actionTitle, final Runnable action) {
        Snackbar.make(findViewById(android.R.id.content), tips,
                infinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT)
                .setAction(actionTitle, action == null ? null : new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        action.run();
                    }
                })
                .show();
    }

    @Override
    public void showTips(@StringRes int tipsRes, boolean infinite, String actionTitle, final Runnable action) {
        Snackbar.make(findViewById(android.R.id.content), tipsRes,
                infinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT)
                .setAction(actionTitle, action == null ? null : new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        action.run();
                    }
                })
                .show();
    }

    @Override
    public void showProgress(@StringRes int progressTitle, @StringRes int progressMessage) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(progressTitle);
        progressDialog.setMessage(getString(progressMessage));
        progressDialog.show();
    }

    @Override
    public void showProgress(CharSequence progressTitle, CharSequence progressMessage) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(progressTitle);
        progressDialog.setMessage(progressMessage);
        progressDialog.show();
    }

    @NonNull
    @Override
    public Context getContext() {
        return this.getApplicationContext();
    }

    @Override
    public void checkRuntimePermissions() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    protected Activity getActivity() {
        return this;
    }
}
