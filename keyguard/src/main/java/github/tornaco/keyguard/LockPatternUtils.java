package github.tornaco.keyguard;

import com.google.android.collect.Lists;

import java.util.List;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class LockPatternUtils {
    /**
     * Serialize a pattern.
     *
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn() + '1');
        }
        return new String(res);
    }

    /**
     * Deserialize a pattern.
     *
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        if (string == null) {
            return null;
        }

        List<LockPatternView.Cell> result = Lists.newArrayList();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = (byte) (bytes[i] - '1');
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }
}
