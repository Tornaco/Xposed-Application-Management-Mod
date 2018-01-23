package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.extra.PayListBrowserActivity;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.AliPayUtil;

public class DonateActivity extends BaseActivity implements SwitchBar.OnSwitchChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);

        setupToolbar();
        showHomeAsUp();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PayListBrowserActivity.class));
            }
        });

        findViewById(R.id.pay_ali).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliPayUtil.startPay(getContext());
            }
        });

        findViewById(R.id.pay_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setImageResource(R.drawable.wechat_pay);
                new AlertDialog.Builder(getActivity())
                        .setView(imageView)
                        .show();
            }
        });

        SwitchBar switchBar = findViewById(R.id.switchbar);
        switchBar.setEnabled(true);
        switchBar.show();
        switchBar.setOnRes(R.string.donated);
        switchBar.setOffRes(R.string.donated);
        switchBar.setTextViewLabel(AppSettings.isDonated(getContext()));
        switchBar.setChecked(AppSettings.isDonated(getContext()));
        switchBar.addOnSwitchChangeListener(this);
    }

    @Override
    public void onSwitchChanged(SwitchCompat switchView, boolean isChecked) {
        AppSettings.setDonated(getContext(), isChecked);
    }
}
