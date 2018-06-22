package github.tornaco.xposedmoduletest.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.net.URISyntaxException;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/13.
 * Email: Tornaco@163.com
 * Ref: http://blog.csdn.net/likesyour/article/details/61198577
 */

public class AliPayUtil {

    private static final String ALIPAY_CODE = "FKX06988GF3TO9COH5IX57";
    private static final String ALIPAY_RED_PACKET_CODE
            = "（X-APM复制）支付宝发红包啦！即日起还有机会额外获得余额宝消费红包！长按复制此消息，打开最新版支付宝就能领取！Hj7xjD69g7";

    private static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";

    private static final String INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";


    public static boolean startPay(Context context) {
        if (!PkgUtil.isPkgInstalled(context, ALIPAY_PACKAGE_NAME)) return false;
        return startPayByIntent(context, INTENT_URL_FORMAT.replace("{urlCode}", ALIPAY_CODE));
    }

    private static boolean startPayByIntent(Context context, String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (URISyntaxException | ActivityNotFoundException e) {
            return false;
        }
    }

    public static boolean getRedPacket(Context context) {
        try {

            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cmb != null) {
                cmb.setPrimaryClip(ClipData.newPlainText("RedPacket-X-APM", ALIPAY_RED_PACKET_CODE));
                Toast.makeText(context, R.string.toast_alipay_red_packet_copied, Toast.LENGTH_SHORT).show();
                PackageManager pm = context.getPackageManager();
                Intent launchIntent = pm.getLaunchIntentForPackage(ALIPAY_PACKAGE_NAME);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return true;
                }
            }

        } catch (ActivityNotFoundException ignored) {
        }
        return false;
    }
}
