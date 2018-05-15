package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Tornaco on 2018/5/15 10:59.
 * God bless no bug!
 */
public class FuturaTextView extends AppCompatTextView {
    public FuturaTextView(Context context) {
        super(context);
    }

    public FuturaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FuturaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/futura-medium-bt.ttf"));
    }
}
