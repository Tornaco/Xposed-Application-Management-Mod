package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.EmojiUtil;

/**
 * Created by guohao4 on 2018/2/6.
 * Email: Tornaco@163.com
 */

public class EmojiViewUtil {

    public static ViewGroup makeMessageViewForDialog(Context context, String message) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context)
                .inflate(R.layout.emoji_textview, null, false);
        TextView textView = viewGroup.findViewById(R.id.content_text);
        textView.setText(EmojiUtil.localReplaceEmojiCode(message));
        return viewGroup;
    }
}
