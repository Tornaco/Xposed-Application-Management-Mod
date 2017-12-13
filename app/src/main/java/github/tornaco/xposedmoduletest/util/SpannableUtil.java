package github.tornaco.xposedmoduletest.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by guohao4 on 2017/12/2.
 * Email: Tornaco@163.com
 */

public class SpannableUtil {

    public static final String PATTERN_MSGID = "msgid=\\\"\\d*\\\"";
    public static final String PATTEN_AT = "@[\\w-·]+";
    public static final String PATTEN_AT_TREND = "(#[^# ']+#)|(@[\\w-·]+)";
    public static final String PATTEN_EMOTION = "\\[(\\S+?)\\]";
    public static final String PATTEN_EMOTION_WITH_TREND_EFFECT = "(<a href='[^>]*?\\[\\S+?\\]+[^>]*?'>)|\\[(\\S+?)\\]";
    public static final String PATTEN_HUAN_HANG = "(?m)^.*$";
    private static final String PATTEN_MUSIC_URL = ".*(music.163|xiami|music.qq|kugou|kuwo|duomi|music.baidu|changba).(com|cn)";
    public static final String PATTEN_QUAN_WEN = "(全文： http://m.weibo.cn/.*|http://m.weibo.cn/client/version.*)";
    public static final String PATTEN_SYSTEM_EMOJI = "[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]";
    public static final String PATTEN_TRANSLATE_SPECIAL = "(\\[(\\S+?)\\])|(#[^# ']+#)|(@[\\w-·]+)|(http[s]*://[[[^/:]&&[a-zA-Z_0-9]]\\.]+(:\\d+)?(/[a-zA-Z_0-9]+)*(/[a-zA-Z_0-9]*([a-zA-Z_0-9]+\\.[a-zA-Z_0-9]+)*)?(\\?(&?[a-zA-Z_0-9]+=[%[a-zA-Z_0-9]-]*)*)*(#[[a-zA-Z_0-9]|-]+)?(.html)?)|([\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff])";
    public static final String PATTEN_TREND = "#[^# ']+#";
    private static final String PATTEN_VIDEO_URL = ".*(tv.sohu|youku|v.qq|v.163|ku6|letv|iqiyi|yinyuetai|kankan|pptv|tudou|fun|cntv|bilibili|meipai).(com|cn|tv)";
    public static final String PATTEN_WEB = "http[s]*://[[[^/:]&&[a-zA-Z_0-9]]\\.]+(:\\d+)?(/[a-zA-Z_0-9]+)*(/[a-zA-Z_0-9]*([a-zA-Z_0-9]+\\.[a-zA-Z_0-9]+)*)?(\\?(&?[a-zA-Z_0-9]+=[%[a-zA-Z_0-9]-]*)*)*(#[[a-zA-Z_0-9]|-]+)?(.html)?";
    public static final String PATTEN_WEB_HTML = "http[s]*://[^\\s]+";
    public static final String PATTEN_WHSPACE = "\\s+";
    public static final String ZH_HANG = "[\\u4e00-\\u9fa5]";

    public static final Pattern atPattern = Pattern.compile(PATTEN_AT);
    public static final Pattern atTrendPattern = Pattern.compile(PATTEN_AT_TREND);
    public static final Pattern comPattern = Pattern.compile("([\\w]+\\.(?:net|org|hk|cn|com\\.cn|com\\.hk|com|net\\.cn|org\\.cn|biz|info|cc|tv|mobi|name|asia|tw|sh|ac|io|tm|travel|ws|us|sc|in|la|in|cm|co|so))(?![\\w]+)");
    public static final Pattern emotionPattern = Pattern.compile(PATTEN_EMOTION_WITH_TREND_EFFECT);
    public static final Pattern huanhangPattern = Pattern.compile(PATTEN_HUAN_HANG);
    public static final Pattern profileQrPattern = Pattern.compile("(http://)?weibo\\.cn/qr/userinfo\\?uid=(\\d+)|(www\\.)?weibo\\.com/(\\d+)/profile|(www\\.)?weibo\\.com/u/(\\d+)");
    public static final Pattern quanwenPattern = Pattern.compile(PATTEN_QUAN_WEN);
    public static final Pattern translatePattern = Pattern.compile(PATTEN_TRANSLATE_SPECIAL);
    public static final Pattern trendPattern = Pattern.compile(PATTEN_TREND);
    public static final Pattern webPattern = Pattern.compile(PATTEN_WEB);
    public static final Pattern whspacePattern = Pattern.compile(PATTEN_WHSPACE);
    public static final Pattern zhPattern = Pattern.compile(ZH_HANG);

    public static SpannableStringBuilder buildHighLightString(Context context,
                                                              int normalColor,
                                                              int highlightColor,
                                                              @StringRes int strResId) {
        String source = context.getString(strResId);
        SpannableStringBuilder ssb = new SpannableStringBuilder(source);

        Matcher matcher = atPattern.matcher(source);
        while (matcher.find()) {
            final String who = matcher.group();
            int length = who.length();
            int startIndex = matcher.start();

            ATFixedClickableSpan t = new ATFixedClickableSpan(
                    normalColor,
                    highlightColor,
                    new FixedClickableSpan
                            .OnClickListener<FixedClickableSpan>() {
                        @Override
                        public void onClick(View widget, FixedClickableSpan span) {
                            ATFixedClickableSpan atClickableSpan = (ATFixedClickableSpan) span;
                        }
                    });
            t.setWho(who);

            ssb.setSpan(t, startIndex, startIndex
                    + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }
}
