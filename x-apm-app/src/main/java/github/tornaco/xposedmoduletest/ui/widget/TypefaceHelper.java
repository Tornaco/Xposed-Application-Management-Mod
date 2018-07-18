package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Tornaco on 2018/6/13 14:07.
 * This file is writen for project X-APM at host guohao4.
 */
public class TypefaceHelper {

    private static Typeface sFutura;

    public static Typeface futura(Context context) {
        synchronized (TypefaceHelper.class) {
            if (sFutura == null) {
                sFutura = Typeface.createFromAsset(context.getAssets(), "fonts/futura-medium-bt.ttf");
            }
            return sFutura;
        }
    }

    public static Typeface googleSans(Context context) {
        synchronized (TypefaceHelper.class) {
            if (sFutura == null) {
                sFutura = Typeface.createFromAsset(context.getAssets(), "fonts/GoogleSans-Regular.ttf");
            }
            return sFutura;
        }
    }
}
