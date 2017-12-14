package github.tornaco.xposedmoduletest.ui.activity.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
public class PrivacySettingsActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy);
        setupToolbar();
        showHomeAsUp();

        if (!XAshmanManager.get().isServiceAvailable()) return;

        PrivacySettingsActivityPermissionRequester.setupViewsChecked(this);
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    void setupViews() {
        setupViews3();
        setupViews2();
        setupViews1();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PrivacySettingsActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void setupViews1() {

        View card = findViewById(R.id.card);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(XAshmanManager.get().getAndroidId());

        card.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        card.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAshmanManager.get().setUserDefinedAndroidId(null);
                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(XAshmanManager.get().getAndroidId());
                    }
                }, 1000);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"MissingPermission", "HardwareIds"})
    void setupViews2() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final View card = findViewById(R.id.card2);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(tm.getDeviceId());

        card.findViewById(R.id.button21).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        card.findViewById(R.id.button22).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAshmanManager.get().setUserDefinedDeviceId(null);

                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(tm.getDeviceId());
                    }
                }, 1000);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"MissingPermission", "HardwareIds"})
    void setupViews3() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final View card = findViewById(R.id.card3);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(tm.getLine1Number());

        card.findViewById(R.id.button31).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        card.findViewById(R.id.button32).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAshmanManager.get().setUserDefinedLine1Number(null);

                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(tm.getLine1Number());
                    }
                }, 1000);
            }
        });
    }

}
