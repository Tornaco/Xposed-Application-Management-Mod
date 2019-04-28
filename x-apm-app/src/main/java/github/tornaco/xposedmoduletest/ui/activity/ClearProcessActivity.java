package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.util.Arrays;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

public class ClearProcessActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.clear_process);
        setTitle(null);
        setupToolbar();
        showHomeAsUp();

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TextView textView = findViewById(R.id.text);

        XAPMManager.get().clearProcess(new IProcessClearListenerAdapter() {

            @Override
            public void onPrepareClearing() throws RemoteException {
                super.onPrepareClearing();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("...");
                    }
                });
            }

            @Override
            public void onClearedPkg(final String pkg) throws RemoteException {
                super.onClearedPkg(pkg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(getString(R.string.clearing_process_app,
                                PkgUtil.loadNameByPkgName(getApplicationContext(), pkg)));
                    }
                });
            }

            @Override
            public void onAllCleared(String[] pkg) throws RemoteException {
                super.onAllCleared(pkg);
                Logger.d("onAllCleared: " + Arrays.toString(pkg));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(R.string.done);
                        fab.show();
                    }
                });
            }
        }, false, false);
    }
}
