package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.TextView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.EmojiUtil;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
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

        final TextView textView = findViewById(R.id.text);

        XAshmanManager.defaultInstance().clearProcess(new IProcessClearListenerAdapter() {
            @Override
            public void onClearedPkg(final String pkg) throws RemoteException {
                super.onClearedPkg(pkg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(getString(R.string.clearing_process,
                                PkgUtil.loadNameByPkgName(getApplicationContext(), pkg)));
                    }
                });
            }

            @Override
            public void onAllCleared(String[] pkg) throws RemoteException {
                super.onAllCleared(pkg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(getString(R.string.done, EmojiUtil.getEmojiByUnicode(EmojiUtil.HAPPY)));
                    }
                });
            }
        });
    }
}
