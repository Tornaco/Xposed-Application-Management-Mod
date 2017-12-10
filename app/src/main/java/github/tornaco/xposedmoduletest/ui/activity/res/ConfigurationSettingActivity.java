package github.tornaco.xposedmoduletest.ui.activity.res;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.CongfigurationSetting;
import github.tornaco.xposedmoduletest.provider.ConfigurationSettingProvider;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/9.
 * Email: Tornaco@163.com
 */

public class ConfigurationSettingActivity extends BaseActivity {

    private static final String EXTRA_PKG = "pkg";

    private String mPackageName;

    public static void start(Context context, String packageName) {
        Intent starter = new Intent(context, ConfigurationSettingActivity.class);
        starter.putExtra(EXTRA_PKG, packageName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.config_settings);

        if (!resolveIntent()) {
            return;
        }

        setupToolbar();
        showHomeAsUp();

        setTitle(PkgUtil.loadNameByPkgName(this, mPackageName));

        setupView();
    }

    private void setupView() {

        final EditText dpiText = findViewById(R.id.edit_text_dpi);
        final EditText fontScaleText = findViewById(R.id.edit_text_font_scale);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String dpitext = dpiText.getText().toString();
                    int dpi = TextUtils.isEmpty(dpitext) ? -1 : Integer.parseInt(dpitext);

                    String scaleText = fontScaleText.getText().toString();
                    float scale = TextUtils.isEmpty(scaleText) ? -1 : Float.parseFloat(scaleText);

                    CongfigurationSetting setting = new CongfigurationSetting();
                    setting.setPackageName(mPackageName);
                    setting.setDensityDpi(dpi);
                    setting.setFontScale(scale/100f);

                    ConfigurationSettingProvider.insertOrUpdate(getContext().getApplicationContext(), setting);

                    Logger.d("Inserting " + setting);

                    finish();

                } catch (Throwable e) {
                    Toast.makeText(getActivity(), Log.getStackTraceString(e), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean resolveIntent() {
        mPackageName = getIntent().getStringExtra(EXTRA_PKG);
        return !TextUtils.isEmpty(mPackageName);
    }
}
