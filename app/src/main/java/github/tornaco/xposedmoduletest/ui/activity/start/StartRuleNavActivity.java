package github.tornaco.xposedmoduletest.ui.activity.start;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.StartRuleLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

public class StartRuleNavActivity extends CommonPackageInfoListActivity
        implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, StartRuleNavActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onRequestClearItemsInBackground() {
        Collections.consumeRemaining(getCommonPackageInfoAdapter().getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo commonPackageInfo) {
                        if (commonPackageInfo.isChecked()) {
                            XAshmanManager.get().addOrRemoveStartRules(commonPackageInfo.getAppName(),
                                    false);
                        }
                    }
                });
    }

    @Override
    protected void onInitSwitchBar(SwitchBar switchBar) {
        switchBar.hide();
    }

    @Override
    protected void onRequestPick() {
        final EditText e = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_new_start_rule)
                .setView(e)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = e.getText().toString();
                                boolean added = XAshmanManager.get().addOrRemoveStartRules(text, true);
                                if (added) {
                                    startLoading();
                                } else {
                                    Toast.makeText(getContext(), R.string.start_rule_add_fail, Toast.LENGTH_SHORT).show();
                                }
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
        return new CommonPackageInfoAdapter(this);
    }

    @Override
    protected List<CommonPackageInfo> performLoading() {
        return StartRuleLoader.Impl.create(this).loadInstalled();
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_rule, menu);
        return super.onCreateOptionsMenu(menu);
    }

}