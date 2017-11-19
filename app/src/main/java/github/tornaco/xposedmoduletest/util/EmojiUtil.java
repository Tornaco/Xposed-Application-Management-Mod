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

    public static final int HAPPY = 0X1F60F;
    public static final int UNHAPPY = 0x1F625;
    public static final int BOOST = 0x1F680;
    public static final int SHEEP = 0x1F40F;

    public static final int HEIHEIHEI = 0x9999;
    public static final int DOG = 0x9998;
    public static final int FIVE_MORE = 0x9996;
    public static final int ZHOUMEI = 0x9995;
    public static final int HONGLIAN = 0x9994;
    public static final int ERHA = 0x9993;

    public static final int[] RANDOM_CANDIDATES = new int[]{
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

    public static String randomEmoji() {
        int r = new Random().nextInt(RANDOM_CANDIDATES.length);
        return getEmojiByUnicode(RANDOM_CANDIDATES[r]);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}
