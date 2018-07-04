package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.loader.PermissionLoader;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.AppOpsTemplateEditorAdapter;
import github.tornaco.xposedmoduletest.util.TextWatcherAdapter;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;

/**
 * Created by Tornaco on 2018/6/15 16:24.
 * This file is writen for project X-APM at host guohao4.
 */
public class AppOpsTemplateEditorActivity extends BaseActivity {

    private EditText mTitleEditable;

    private AppOpsTemplate mOpsTemplate;

    private AppOpsTemplateEditorAdapter permissionOpsAdapter;

    public static void start(Context context, AppOpsTemplate template) {
        Intent starter = new Intent(context, AppOpsTemplateEditorActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra("template", template);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appops_template_editor);
        setupToolbar();
        showHomeAsUp();

        mOpsTemplate = getIntent().getParcelableExtra("template");
        if (mOpsTemplate == null) {
            mOpsTemplate = new AppOpsTemplate();
            mOpsTemplate.setAlias("No name");
        }

        mTitleEditable = findViewById(R.id.toolbar_title);
        mTitleEditable.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mOpsTemplate.setAlias(getCurrentEditingTitle());
            }
        });

        setTitle(mOpsTemplate.getAlias());

        Toolbar editorActionToolbar = findViewById(R.id.editor_actions_toolbar);
        editorActionToolbar.inflateMenu(R.menu.appops_template_editor_actions);
        editorActionToolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_save_apply) {
                inflateTemplateWithUserSettings();
                XAPMManager.get().addAppOpsTemplate(mOpsTemplate);
                Toast.makeText(getContext(), R.string.title_app_ops_template_saved, Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            if (item.getItemId() == R.id.action_save) {
                inflateTemplateWithUserSettings();
                XAPMManager.get().addAppOpsTemplate(mOpsTemplate);
                Toast.makeText(getContext(), R.string.title_app_ops_template_saved, Toast.LENGTH_SHORT).show();
                return true;
            }

            if (item.getItemId() == R.id.action_discard) {
                finish();
                return true;
            }
            return false;
        });

        SwitchCompat switchCompat = new SwitchCompat(getActivity());
        switchCompat.setText(R.string.title_app_ops_template_edit_apply_batch);
        switchCompat.setChecked(true);
        switchCompat.setOnClickListener(v -> {
            boolean checked = switchCompat.isChecked();
            applyBatch(checked ? XAppOpsManager.MODE_ALLOWED : XAppOpsManager.MODE_IGNORED);
        });
        editorActionToolbar.addView(switchCompat);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        permissionOpsAdapter = onCreateAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(permissionOpsAdapter);

        startLoading();
    }

    private void inflateTemplateWithUserSettings() {
        List<Permission> permissions = permissionOpsAdapter.getData();
        for (Permission p : permissions) {
            mOpsTemplate.setMode(p.getCode(), p.getMode());
        }
    }

    private void applyBatch(int mode) {
        List<Permission> permissions = permissionOpsAdapter.getData();
        for (Permission p : permissions) {
            p.setMode(mode);
        }
        permissionOpsAdapter.notifyDataSetChanged();
    }

    protected void startLoading() {
        XExecutor.execute(() -> {
            final List<Permission> res = performLoading();
            // Apply mode.
            for (Permission p : res) {
                p.setMode(mOpsTemplate.getMode(p.getCode()));
            }
            runOnUiThread(() -> permissionOpsAdapter.update(res));
        });
    }

    protected List<Permission> performLoading() {
        return PermissionLoader.Impl.create(this).load(null, 0, CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP);
    }

    protected AppOpsTemplateEditorAdapter onCreateAdapter() {
        return new AppOpsTemplateEditorAdapter(this);
    }

    @Override
    public void setTitle(int titleId) {
        // super.setTitle(titleId);
        setTitleInternal(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        // super.setTitle(title);
        setTitleInternal(title);
    }

    private void setTitleInternal(CharSequence title) {
        mTitleEditable.setText(title);
    }

    private String getCurrentEditingTitle() {
        return String.valueOf(mTitleEditable.getText().toString()).trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
