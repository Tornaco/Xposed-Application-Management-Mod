package github.tornaco.xposedmoduletest.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.tiles.Advanced;
import github.tornaco.xposedmoduletest.ui.tiles.Experiment;
import github.tornaco.xposedmoduletest.ui.tiles.Secure;
import github.tornaco.xposedmoduletest.ui.tiles.Verifier;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class SettingsDashboardActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    public static class Dashboards extends DashboardFragment {
        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category personal = new Category();
            personal.titleRes = R.string.title_personal;
            personal.addTile(new Secure(getActivity()));
            personal.addTile(new Verifier(getActivity()));
            personal.addTile(new Advanced(getActivity()));

            Category others = new Category();
            others.titleRes = R.string.title_others;
            others.addTile(new Experiment(getActivity()));

            categories.add(personal);
            categories.add(others);
        }
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
}
