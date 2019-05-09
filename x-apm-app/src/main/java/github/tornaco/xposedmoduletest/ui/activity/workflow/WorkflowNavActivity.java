package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.common.io.Closer;

import org.newstand.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.JsLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import lombok.Getter;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

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
        Collections.consumeRemaining(getCommonPackageInfoAdapter()
                        .getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        JavaScript js = (JavaScript) commonPackageInfo.getArgs();
                        XAPMManager.get().deleteJs(js);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected void onRequestPick() {
        WorkflowEditorActivity.start(getContext(), null);
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

                holder.getCheckableImageView().setImageDrawable(ContextCompat
                        .getDrawable(getContext(), R.mipmap.ic_workflow));

                WorkflowItemViewHolder workflowItemViewHolder = (WorkflowItemViewHolder) holder;
                workflowItemViewHolder.getRunView().setOnClickListener(v ->
                        XAPMManager.get().evaluateJsString(new String[]{js.getScript()}, new DialogEvaluateListener(getActivity())));
            }

            @Override
            protected CommonViewHolder onCreateViewHolder(View root) {
                return new WorkflowItemViewHolder(root);
            }

            @Override
            protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
                super.onItemClickNoneChoiceMode(commonPackageInfo, view);
                JavaScript js = (JavaScript) commonPackageInfo.getArgs();
                WorkflowEditorActivity.start(getContext(), js);
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

        if (javaScripts.size() == 0) {
            JavaScript example = new JavaScript();

            Closer closer = Closer.create();
            try {
                InputStream is = getAssets().open("js/demo.js");
                StringBuilder rawScript = new StringBuilder();
                BufferedReader br = closer.register(new BufferedReader(new InputStreamReader(is)));
                String line;
                while ((line = br.readLine()) != null) {
                    rawScript.append(line).append("\n");
                }

                Logger.d(rawScript);

                example.setScript(rawScript.toString());
                example.setAlias("EXAMPLE");
                example.setCreatedAt(System.currentTimeMillis());
                example.setId(UUID.randomUUID().toString());

                javaScripts.add(example);
            } catch (IOException ignored) {
            } finally {
                github.tornaco.xposedmoduletest.xposed.util.Closer.closeQuietly(closer);
            }
        }

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
        getMenuInflater().inflate(R.menu.workflow_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Getter
    class WorkflowItemViewHolder extends CommonPackageInfoAdapter.CommonViewHolder {
        private View runView;

        WorkflowItemViewHolder(View itemView) {
            super(itemView);
            runView = itemView.findViewById(R.id.btn_run);
        }
    }
}