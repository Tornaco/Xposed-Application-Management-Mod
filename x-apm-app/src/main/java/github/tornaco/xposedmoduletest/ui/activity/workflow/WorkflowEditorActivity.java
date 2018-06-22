package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.util.TextWatcherAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;

/**
 * Created by Tornaco on 2018/6/15 16:24.
 * This file is writen for project X-APM at host guohao4.
 */
public class WorkflowEditorActivity extends BaseActivity {

    private EditText mContentEditable, mTitleEditable;

    private JavaScript mInputJs;

    public static void start(Context context, JavaScript script) {
        Intent starter = new Intent(context, WorkflowEditorActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra("js", script);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workflow_editor);
        setupToolbar();
        showHomeAsUp();

        mInputJs = getIntent().getParcelableExtra("js");
        if (mInputJs == null) {
            mInputJs = new JavaScript();
            mInputJs.setId(UUID.randomUUID().toString());
            mInputJs.setScript(null);
            mInputJs.setCreatedAt(System.currentTimeMillis());
            mInputJs.setAlias("No name");
        }

        mContentEditable = findViewById(R.id.edit_text);
        mContentEditable.setText(mInputJs.getScript());
        mContentEditable.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mInputJs.setScript(getCurrentEditingContent());
            }
        });

        mTitleEditable = findViewById(R.id.toolbar_title);
        mTitleEditable.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mInputJs.setAlias(getCurrentEditingTitle());
            }
        });

        setTitle(mInputJs.getAlias());

        Toolbar editorActionToolbar = findViewById(R.id.editor_actions_toolbar);
        editorActionToolbar.inflateMenu(R.menu.workflow_editor_actions);
        editorActionToolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_copy) {
                return true;
            }

            if (item.getItemId() == R.id.action_run) {
                XAPMManager.get().evaluateJsString(new String[]{getCurrentEditingContent()}, new DialogEvaluateListener(getActivity()));
                return true;
            }

            if (item.getItemId() == R.id.action_save_apply) {
                XAPMManager.get().saveJs(mInputJs);
                Toast.makeText(getContext(), R.string.title_workflow_saved, Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            if (item.getItemId() == R.id.action_save) {
                XAPMManager.get().saveJs(mInputJs);
                Toast.makeText(getContext(), R.string.title_workflow_saved, Toast.LENGTH_SHORT).show();
                return true;
            }

            if (item.getItemId() == R.id.action_discard) {
                finish();
                return true;
            }

            if (item.getItemId() == R.id.action_arrow_left) {
                return true;
            }
            return false;
        });
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

    private String getCurrentEditingContent() {
        return String.valueOf(mContentEditable.getText().toString()).trim();
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
