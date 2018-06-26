package github.tornaco.xposedmoduletest.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.widget.ImageView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.extra.PayListBrowserActivity;
import github.tornaco.xposedmoduletest.ui.widget.SwitchBar;
import github.tornaco.xposedmoduletest.util.AliPayUtil;

public class DonateActivity extends BaseActivity implements SwitchBar.OnSwitchChangeListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, DonateActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);

        setupToolbar();
        showHomeAsUp();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(getContext(), PayListBrowserActivity.class)));

        findViewById(R.id.pay_ali).setOnClickListener(v -> AliPayUtil.startPay(getContext()));

        findViewById(R.id.pay_wechat).setOnClickListener(v -> {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(R.drawable.wechat_pay);
            new AlertDialog.Builder(getActivity())
                    .setView(imageView)
                    .show();
        });

        findViewById(R.id.pay_btc).setOnClickListener(v -> copyTextToClipBoard("btc", "1GTYfybX6WjbSDPAwpuAu4qKocsTAEMrUN"));

        findViewById(R.id.pay_eth).setOnClickListener(v -> copyTextToClipBoard("eth", "0xf2a73cc9b369b125f435878526eeb640c850369e"));

        findViewById(R.id.pay_ali_red_pkt).setOnClickListener(v -> AliPayUtil.getRedPacket(getContext()));

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

    public void navigateToWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void copyTextToClipBoard(final String tag, final String text) {
        showTips(getString(R.string.message_coin_address_copied, tag, text), false,
                getString(android.R.string.copy), () -> {
                    ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cmb != null) {
                        cmb.setPrimaryClip(ClipData.newPlainText(tag, text));
                    }
                });
    }
}
