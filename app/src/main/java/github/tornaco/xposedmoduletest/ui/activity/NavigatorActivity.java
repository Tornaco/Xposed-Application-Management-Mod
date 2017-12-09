package github.tornaco.xposedmoduletest.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.PowerManagerCompat;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.app.AboutDashboardActivity;
import github.tornaco.xposedmoduletest.ui.activity.app.AppDashboardActivity;
import github.tornaco.xposedmoduletest.ui.tiles.AppBoot;
import github.tornaco.xposedmoduletest.ui.tiles.AppGuard;
import github.tornaco.xposedmoduletest.ui.tiles.AppStart;
import github.tornaco.xposedmoduletest.ui.tiles.CompReplacement;
import github.tornaco.xposedmoduletest.ui.tiles.ComponentManager;
import github.tornaco.xposedmoduletest.ui.tiles.LockKill;
import github.tornaco.xposedmoduletest.ui.tiles.NFManager;
import github.tornaco.xposedmoduletest.ui.tiles.RFKill;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class NavigatorActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigator);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initFirstRun();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                onCreateFragment()).commitAllowingStateLoss();
    }

    private void initFirstRun() {
        boolean first = AppSettings.isFirstRun(this);
        if (first) {
            new AlertDialog.Builder(NavigatorActivity.this)
                    .setTitle(R.string.title_app_update_log)
                    .setMessage(getString(R.string.update_log_212)
                            + "\n\n" + getString(R.string.message_first_run))
                    .setCancelable(false)
                    .setNeutralButton(R.string.no_remind, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppSettings.setFirstRun(getApplicationContext());
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, AppDashboardActivity.class));
        }
        if (item.getItemId() == R.id.action_help) {
            navigateToWebPage(getString(R.string.app_wiki_url));
        }
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutDashboardActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    protected Fragment onCreateFragment() {
        return new NavigatorFragment();
    }

    public static class NavigatorFragment extends DashboardFragment {
        @Getter
        private View rootView;

        @Override
        protected int getLayoutId() {
            return R.layout.fragment_navigator;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
            setupView();
            return rootView;
        }

        private void setupView() {

            TextView statusTitle = findView(rootView, android.R.id.title);
            ImageView imageView = findView(rootView, R.id.icon1);

            ViewGroup btnContainer = findView(rootView, R.id.button_container);
            Button button = findView(rootView, R.id.button);

            boolean isNewBuild = AppSettings.isNewBuild(getActivity());
            btnContainer.setVisibility(View.GONE);

            statusTitle.setText(isServiceAvailable() ?
                    R.string.title_service_connected : R.string.title_service_not_connected);

            TextView summaryView = findView(rootView, android.R.id.text1);

            if (isNewBuild) {
                summaryView.setText(R.string.app_intro_need_restart);
                ViewGroup header = findView(rootView, R.id.header1);
                header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.amber));
                imageView.setImageResource(R.drawable.ic_error_black_24dp);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PowerManagerCompat.restartAndroid();
                    }
                });
            } else {
                summaryView.setText(R.string.app_intro);
                ViewGroup header = findView(rootView, R.id.header1);
                header.setBackgroundColor(
                        XAppGuardManager.get().isServiceAvailable() ?
                                ContextCompat.getColor(getActivity(), R.color.green)
                                : ContextCompat.getColor(getActivity(), R.color.red));
                imageView.setImageResource(isServiceAvailable()
                        ? R.drawable.ic_check_circle_black_24dp
                        : R.drawable.ic_error_black_24dp);
            }
        }

        private boolean isServiceAvailable() {
            return XAppGuardManager.get().isServiceAvailable();
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category category = new Category();
            category.titleRes = R.string.title_secure;
            category.addTile(new AppGuard(getActivity()));

            Category rest = new Category();
            rest.titleRes = R.string.title_restrict;
            rest.addTile(new AppBoot(getActivity()));
            rest.addTile(new AppStart(getActivity()));
            rest.addTile(new LockKill(getActivity()));
            rest.addTile(new RFKill(getActivity()));

            Category ash = new Category();
            ash.titleRes = R.string.title_control;
            ash.addTile(new ComponentManager(getActivity()));
            ash.addTile(new CompReplacement(getActivity()));
            ash.addTile(new NFManager(getActivity()));

            categories.add(category);
            categories.add(rest);
            categories.add(ash);
        }

        @SuppressWarnings("unchecked")
        protected <T extends View> T findView(@IdRes int idRes) {
            return (T) getRootView().findViewById(idRes);
        }

        @SuppressWarnings("unchecked")
        protected <T extends View> T findView(View root, @IdRes int idRes) {
            return (T) root.findViewById(idRes);
        }

    }
}
