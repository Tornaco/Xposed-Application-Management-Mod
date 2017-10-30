package github.tornaco.xposedmoduletest.x;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class XEnc {

    public static boolean isPassCodeCorrect(String codeEnc, String codeInput) {
        return codeEnc != null && codeEnc.equals(codeInput);
    }

    public static boolean isPassCodeValid(String codeEnc) {
        return codeEnc != null && codeEnc.length() == XSettings.PASSCODE_LEVEL;
    }
}
