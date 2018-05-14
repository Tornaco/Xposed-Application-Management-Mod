package github.tornaco.xposedmoduletest.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import java.net.URISyntaxException;

import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/13.
 * Email: Tornaco@163.com
 * Ref: http://blog.csdn.net/likesyour/article/details/61198577
 */

public class AliPayUtil {

    private static final String ALIPAY_CODE = "FKX06988GF3TO9COH5IX57";

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
}
