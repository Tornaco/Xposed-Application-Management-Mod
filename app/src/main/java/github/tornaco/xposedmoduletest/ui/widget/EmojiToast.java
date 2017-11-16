package github.tornaco.xposedmoduletest.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

public class EmojiToast {

    public static void show(Context context, String content) {
        Toast toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.toast_container,
                null, false);
        toast.setView(view);
        TextView textView = view.findViewById(R.id.content_text);
        textView.setText(content);
        toast.show();
    }
}
