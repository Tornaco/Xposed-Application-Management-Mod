package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.tiles.AppBoot;
import github.tornaco.xposedmoduletest.ui.tiles.AppGuard;
import github.tornaco.xposedmoduletest.ui.tiles.AppStart;
import github.tornaco.xposedmoduletest.ui.tiles.LockKill;
import github.tornaco.xposedmoduletest.util.EmojiUtil;
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
        setContentView(R.layout.activity_with_container_with_appbar_template);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                onCreateFragment()).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_open_source) {
            navigateToWebPage(getString(R.string.app_source_url));
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
            findView(rootView, R.id.button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WithWithCustomTabActivity withWithCustomTabActivity = (WithWithCustomTabActivity) getActivity();
                            withWithCustomTabActivity.navigateToWebPage(getString(R.string.app_wiki_url));
                        }
                    });

            TextView statusTitle = findView(rootView, android.R.id.title);
            statusTitle.setText(isServiceAvailable() ?
                    R.string.title_service_connected : R.string.title_service_not_connected);
            ViewGroup header = findView(rootView, R.id.header1);
            header.setBackgroundColor(
                    XAppGuardManager.defaultInstance().isServiceAvailable() ?
                            ContextCompat.getColor(getActivity(), R.color.green)
                            : ContextCompat.getColor(getActivity(), R.color.red));

            EmojiTextView emojiTextView = findView(rootView, R.id.icon1);
            emojiTextView.setText(EmojiUtil.getEmojiByUnicode(
                    isServiceAvailable() ?
                            EmojiUtil.HAPPY
                            : EmojiUtil.UNHAPPY));
        }

        private boolean isServiceAvailable() {
            return XAppGuardManager.defaultInstance().isServiceAvailable();
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);
            Category category = new Category();
            category.titleRes = R.string.title_secure;
            category.addTile(new AppGuard(getActivity()));

            Category ash = new Category();
            ash.titleRes = R.string.title_clearn;
            ash.addTile(new AppBoot(getActivity()));
            ash.addTile(new AppStart(getActivity()));
            ash.addTile(new LockKill(getActivity()));

            categories.add(category);
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
