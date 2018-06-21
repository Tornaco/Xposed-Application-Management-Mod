package github.tornaco.xposedmoduletest.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.View;
import lombok.Getter;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/9/21.
 * Email: Tornaco@163.com
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements View {

    private Handler uiThreadHandler;

    protected Themes mUserTheme;

    @Getter
    private boolean isVisible;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserTheme = getUserSetTheme();
        setTheme(getUserSetThemeResId(mUserTheme));
        isVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isVisible = false;
    }


    public Themes getUserSetTheme() {
        return XSettings.getThemes(this.getContext());
    }

    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleResNoActionBar();
    }

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
        Toolbar toolbar = findViewById(resId);
        if (toolbar != null) setSupportActionBar(toolbar);
    }

    @Override
    public void showHomeAsUp() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void showSimpleDialog(String title, String message) {
        new AlertDialog.Builder(BaseActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void showDialog(int title, String message, int positive, final int negative, int neutral, boolean cancelable,
                           @Nullable final Runnable ok, @Nullable final Runnable cancel, @Nullable final Runnable net) {
        new AlertDialog.Builder(BaseActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ok != null) ok.run();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancel != null) cancel.run();
                    }
                })
                .setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (net != null) net.run();
                    }
                })
                .show();

    }

    @Override
    public void showDialog(String title, String message, String positive, String negative,
                           final boolean cancelable,
                           @Nullable final Runnable ok, @Nullable final Runnable cancel) {
        new AlertDialog.Builder(BaseActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ok != null) ok.run();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancel != null) cancel.run();
                    }
                })
                .show();
    }

    @Override
    public void showTips(CharSequence tips, boolean infinite, String actionTitle, final Runnable action) {
        android.view.View base = findViewById(R.id.fab);
        if (base == null) base = findViewById(android.R.id.content);
        Snackbar.make(base, tips,
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
        android.view.View base = findViewById(R.id.fab);
        if (base == null) base = findViewById(android.R.id.content);
        Snackbar.make(base, tipsRes,
                infinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT)
                .setAction(actionTitle, action == null ? null :
                        new android.view.View.OnClickListener() {
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

    /**
     * Show fragment page by replaceV4 the given containerId, if you have data to set
     * give a bundle.
     *
     * @param containerId The id to replaceV4.
     * @param fragment    The fragment to show.
     * @param bundle      The data of the fragment if it has.
     */
    protected boolean replaceV4(final int containerId,
                                Fragment fragment, Bundle bundle) {
        return replaceV4(containerId, fragment, bundle, true);
    }

    /**
     * Show fragment page by replaceV4 the given containerId, if you have data to set
     * give a bundle.
     *
     * @param containerId The id to replaceV4.
     * @param f           The fragment to show.
     * @param bundle      The data of the fragment if it has.
     * @param animate     True if you want to animate the fragment.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean replaceV4(final int containerId,
                                Fragment f, Bundle bundle, boolean animate) {

        if (isDestroyed() || f == null) {
            return false;
        }

        if (bundle != null) {
            f.setArguments(bundle);
        }

        if (!animate) {
            getSupportFragmentManager().beginTransaction()
                    .replace(containerId, f).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(containerId, f)
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .commit();
        }
        return true;
    }

    /**
     * Remove a fragment that is attached, with animation.
     *
     * @param f The fragment to removeV4.
     * @return True if successfully removed.
     * @see #removeV4(Fragment, boolean)
     */
    protected boolean removeV4(final Fragment f) {
        return removeV4(f, true);
    }

    /**
     * Remove a fragment that is attached.
     *
     * @param f       The fragment to removeV4.
     * @param animate True if you want to animate the fragment.
     * @return True if successfully removed.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean removeV4(final Fragment f, boolean animate) {

        if (!isDestroyed() || f == null) {
            return false;
        }

        if (!animate) {
            getSupportFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .remove(f)
                    .commitAllowingStateLoss();//TODO Ignore the result?
        }
        return true;
    }

    public void runOnUiThreadChecked(final Runnable runnable) {
        if (isDestroyed()) return;
        runOnUiThread(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                Logger.e("runOnUiThreadChecked: " + e);
            }
        });
    }

    public void startActivityChecked(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.error_activity_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void setSubTitleChecked(CharSequence title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(title);
        }
    }

    protected RelativeLayout.LayoutParams generateCenterParams() {
        return generateCenterParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    protected RelativeLayout.LayoutParams generateCenterParams(int w, int h) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }
}
