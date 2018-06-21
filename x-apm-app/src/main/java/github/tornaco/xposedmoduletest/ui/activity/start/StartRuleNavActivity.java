package github.tornaco.xposedmoduletest.ui.activity.start;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.StartRuleLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

public class StartRuleNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, StartRuleNavActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                commonPackageInfo -> {
                    if (commonPackageInfo.isChecked()) {
                        XAPMManager.get().addOrRemoveStartRules(commonPackageInfo.getAppName(),
                                false);
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.show();
        switchBar.setChecked(XAPMManager.get().isStartRuleEnabled());
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    protected void onRequestPick() {
        final EditText e = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_create_new_rule)
                .setView(e)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            String text = e.getText().toString();
                            boolean added = XAPMManager.get().addOrRemoveStartRules(text, true);
                            if (added) {
                                startLoading();
                            } else {
                                Toast.makeText(getContext(), R.string.err_rule_add_fail, Toast.LENGTH_SHORT).show();
                            }
                        })
                .show();
    }

    @Override
    protected int getSummaryRes() {
        return R.string.summary_start_app_rules;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoAdapter(this) {
            @Override
            protected boolean imageLoadingEnabled() {
                return false;
            }

            @Override
            public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.getCheckableImageView().setImageDrawable(ContextCompat
                        .getDrawable(getContext(), R.mipmap.ic_rules));
            }
        };
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return StartRuleLoader.Impl.create(this).loadInstalled();
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        XAPMManager.get().setStartRuleEnabled(isChecked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_rule, menu);
        return super.onCreateOptionsMenu(menu);
    }

}