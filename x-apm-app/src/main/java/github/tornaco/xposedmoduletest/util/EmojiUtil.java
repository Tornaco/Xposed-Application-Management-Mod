package github.tornaco.xposedmoduletest.util;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Random;

/**
 * Created by Nick@NewStand.org on 2017/5/2 11:13
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class EmojiUtil {


    public static final int _BASE = 0X1F60F;
    public static final int HAPPY = _BASE + 1;
    public static final int UNHAPPY = _BASE + 2;
    public static final int BOOST = _BASE + 3;
    public static final int SHEEP = _BASE + 4;

    public static final int HEIHEIHEI = _BASE + 5;
    public static final int DOG = _BASE + 6;
    public static final int FIVE_MORE = _BASE + 7;
    public static final int ZHOUMEI = _BASE + 8;
    public static final int HONGLIAN = _BASE + 9;
    public static final int ERHA = _BASE + 10;

    public static final int HEART = _BASE + 11;
    public static final int HEART_BREAK = _BASE + 12;


    private static final int[] RANDOM_CANDIDATES = new int[]{
            HAPPY,
            UNHAPPY,
            BOOST, SHEEP,
            HEIHEIHEI,
            DOG,
            FIVE_MORE,
            ZHOUMEI,
            HONGLIAN,
            ERHA
    };

    private static final int[] ALL = new int[]{
            HAPPY,
            UNHAPPY,
            BOOST, SHEEP,
            HEIHEIHEI,
            DOG,
            FIVE_MORE,
            ZHOUMEI,
            HONGLIAN,
            ERHA
    };

    public static String localReplaceEmojiCode(String from) {
        for (int code : ALL) {
            boolean contains = from.contains(String.valueOf(code));
            from = from.replace(String.valueOf(code), getEmojiByUnicode(code));
        }
        return from;
    }

    public static String randomEmoji() {
        int r = new Random().nextInt(RANDOM_CANDIDATES.length);
        return getEmojiByUnicode(RANDOM_CANDIDATES[r]);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static String contactEmojiByUnicode(int... unicode) {
        StringBuilder emojiSB = new StringBuilder();
        for (int c : unicode) {
            emojiSB.append(getEmojiByUnicode(c));
        }
        return emojiSB.toString();
    }
}
