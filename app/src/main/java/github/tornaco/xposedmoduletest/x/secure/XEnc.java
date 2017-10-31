package github.tornaco.xposedmoduletest.x.secure;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class XEnc {

    static final int PASSCODE_LEVEL = 4;

    public static boolean isPassCodeCorrect(String codeEnc, String codeInput) {
        return codeEnc != null && codeEnc.equals(codeInput);
    }

    public static boolean isPassCodeValid(String codeEnc) {
        return codeEnc != null && codeEnc.length() == PASSCODE_LEVEL;
    }
}
