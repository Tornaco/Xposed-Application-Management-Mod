package github.tornaco.xposedmoduletest.ui.activity.workflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;

/**
 * Created by Tornaco on 2018/6/15 16:24.
 * This file is writen for project X-APM at host guohao4.
 */
public class WorkflowEditorActivity extends BaseActivity {

    private EditText mEditable;

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

        mEditable = findViewById(R.id.edit_text);
        mEditable.setText(mInputJs.getScript());
        mEditable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Noop.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Noop.
            }

            @Override
            public void afterTextChanged(Editable s) {
                mInputJs.setScript(getCurrentEditingContent());
            }
        });

        setTitle(mInputJs.getAlias());
        if (BuildConfig.DEBUG) {
            setSubTitleChecked(mInputJs.getId());
        }
    }

    private String getCurrentEditingContent() {
        return String.valueOf(mEditable.getText().toString()).trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workflow_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_run) {
            XAPMManager.get().evaluateJsString(new String[]{getCurrentEditingContent()});
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            XAPMManager.get().saveJs(mInputJs);
            Toast.makeText(getContext(), R.string.title_workflow_saved, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
