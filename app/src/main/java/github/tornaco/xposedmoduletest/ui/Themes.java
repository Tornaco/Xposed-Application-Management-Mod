package github.tornaco.xposedmoduletest.ui;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */

public enum Themes {

    RED(R.color.red),
    BLUE(R.color.blue),
    INDIGO(R.color.indigo),
    GREEN(R.color.green),
    PINK(R.color.pink),
    ORANGE(R.color.orange),
    BLACK(R.color.black),
    DEFAULT(R.color.primary);

    int themeColorRes;

    Themes(int themeColorRes) {
        this.themeColorRes = themeColorRes;
    }

    public int getThemeColorRes() {
        return themeColorRes;
    }

    public static Themes valueOfChecked(String name) {
        for (Themes t : values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        return DEFAULT;
    }
}
