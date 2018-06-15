package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.loader.JsLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class WorkflowNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, WorkflowNavActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list_workflow;
    }

    @Override
    protected void onRequestClearItemsInBackground() {

    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected void onRequestPick() {

    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_workflow;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this) {
            @Override
            protected int getTemplateLayoutRes() {
                return R.layout.app_list_item_workflow;
            }

            @Override
            public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                JavaScript js = (JavaScript) packageInfo.getArgs();
                holder.getLineTwoTextView().setText(getContext()
                        .getString(R.string.summary_created_at, FuzzyDateTimeFormatter.getTimeAgo(getContext(),
                                new Date(js.getCreatedAt()))));

                GlideApp.with(getContext())
                        .load(packageInfo)
                        .placeholder(0)
                        .error(R.mipmap.ic_launcher_round)
                        .fallback(R.mipmap.ic_launcher_round)
                        .transition(withCrossFade())
                        .into(holder.getCheckableImageView());
            }

            @Override
            protected boolean imageLoadingEnabled() {
                return false;
            }
        };
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        // Wrap to common package info.
        List<JavaScript> javaScripts = JsLoader.Impl.create(getContext()).loadAll();
        List<CommonPackageInfo> res = new ArrayList<>(javaScripts.size());
        for (JavaScript js : javaScripts) {
            CommonPackageInfo c = new CommonPackageInfo();
            c.setPkgName(BuildConfig.APPLICATION_ID);
            c.setChecked(false);
            c.setAppName(js.getAlias());
            c.setArgs(js);
            res.add(c);
        }
        return res;
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}