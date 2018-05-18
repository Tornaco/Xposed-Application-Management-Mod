package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */

class ClickableToastManager {

    public interface OnToastClickListener {
        void onToastClick(String text);
    }

    private static Toast sToast = null;

    public static void show(Context context, final String content,
                            final String contentRaw,
                            final OnToastClickListener listener) {

        // Cancel previous toast first.
        try {

            if (sToast != null) {
                sToast.cancel();
            }

            Toast toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setBackgroundResource(android.R.drawable.toast_frame);
            linearLayout.setFocusable(true);
            linearLayout.setClickable(true);
            TextView textView = new TextView(context);
            textView.setFocusable(true);
            textView.setClickable(true);
            textView.setTextColor(Color.WHITE);
            linearLayout.addView(textView);
            toast.setView(linearLayout);
            textView.setText(content);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToastClick(contentRaw);
                }
            };

            textView.setOnClickListener(clickListener);
            linearLayout.setOnClickListener(clickListener);
            toast.show();

            sToast = toast;
        } catch (Throwable ignored) {
            Log.e(XposedLog.TAG, "ClickableToastManager: " + Log.getStackTraceString(ignored));
        }
    }
}
