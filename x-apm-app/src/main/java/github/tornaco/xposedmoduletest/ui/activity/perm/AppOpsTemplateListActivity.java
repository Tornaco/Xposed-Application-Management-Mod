package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.AppOpsTemplateLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import lombok.Getter;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by Tornaco on 2018/6/29 15:56.
 * This file is writen for project X-APM at host guohao4.
 */
public class AppOpsTemplateListActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, AppOpsTemplateListActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.app_list_appops_template_list;
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter()
                        .getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        AppOpsTemplate template = (AppOpsTemplate) commonPackageInfo.getArgs();
                        XAPMManager.get().removeAppOpsTemplate(template);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected void onRequestPick() {
        AppOpsTemplateEditorActivity.start(getContext(), null);
    }

    @Override
    protected int getSummaryRes() {
        return -1;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this) {
            @Override
            protected int getTemplateLayoutRes() {
                return R.layout.app_list_item_appops_template;
            }

            @Override
            public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                AppOpsTemplate template = (AppOpsTemplate) packageInfo.getArgs();

                holder.getLineTwoTextView().setText(getContext()
                        .getString(R.string.summary_created_at, FuzzyDateTimeFormatter.getTimeAgo(getContext(),
                                new Date(template.getCreatedAtMills()))));

                holder.getCheckableImageView().setImageDrawable(ContextCompat
                        .getDrawable(getContext(), R.mipmap.ic_rules));

                OpsTemplateItemViewHolder opsTemplateItemViewHolder = (OpsTemplateItemViewHolder) holder;
                opsTemplateItemViewHolder.getMoreView().setOnClickListener(v -> {
                    // Noop.
                });
            }

            @Override
            protected CommonViewHolder onCreateViewHolder(View root) {
                return new OpsTemplateItemViewHolder(root);
            }

            @Override
            protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
                super.onItemClickNoneChoiceMode(commonPackageInfo, view);
                AppOpsTemplate opsTemplate = (AppOpsTemplate) commonPackageInfo.getArgs();
                AppOpsTemplateEditorActivity.start(getContext(), opsTemplate);
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
        List<AppOpsTemplate> appOpsTemplateList = AppOpsTemplateLoader.Impl.create(getContext()).loadAll();
        List<CommonPackageInfo> res = new ArrayList<>(appOpsTemplateList.size());
        for (AppOpsTemplate template : appOpsTemplateList) {
            CommonPackageInfo c = new CommonPackageInfo();
            c.setPkgName(BuildConfig.APPLICATION_ID);
            c.setChecked(false);
            c.setAppName(template.getAlias());
            c.setArgs(template);
            res.add(c);
        }
        return res;
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_ops_template_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Getter
    class OpsTemplateItemViewHolder extends CommonPackageInfoAdapter.CommonViewHolder {
        private View moreView;

        OpsTemplateItemViewHolder(View itemView) {
            super(itemView);
            moreView = itemView.findViewById(R.id.btn_more);
            moreView.setVisibility(View.INVISIBLE);
        }
    }
}
