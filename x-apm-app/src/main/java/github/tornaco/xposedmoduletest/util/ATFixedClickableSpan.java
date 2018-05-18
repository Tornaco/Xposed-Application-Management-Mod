package github.tornaco.xposedmoduletest.util;

/**
 * Created by guohao4 on 2017/9/18.
 * Email: Tornaco@163.com
 */

class ATFixedClickableSpan extends FixedClickableSpan {

    private String who;

    ATFixedClickableSpan(int normalColor, int pressedColor, OnClickListener<FixedClickableSpan> onClickListener) {
        super(normalColor, pressedColor, onClickListener);
    }

    String getWho() {
        return who;
    }

    void setWho(String who) {
        this.who = who;
    }

}
