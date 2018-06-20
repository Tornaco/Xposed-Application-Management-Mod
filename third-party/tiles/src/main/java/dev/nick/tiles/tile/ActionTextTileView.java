package dev.nick.tiles.tile;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.nick.tiles.R;

/**
 * Created by nick on 11/12/15.
 */
public class ActionTextTileView extends TileView {

    private TextView actionTextView;
    private View containerView;

    public ActionTextTileView(Context context) {
        super(context);
    }

    public ActionTextTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindActionView(RelativeLayout container) {
        final TextView textView = (TextView) LayoutInflater.from(getContext())
                .inflate(R.layout.action_text_view, container, false);
        textView.setSoundEffectsEnabled(false);
        textView.setText(getActionText(getContext()));

        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int resId = typedValue.resourceId;
        textView.setTextColor(ContextCompat.getColor(getContext(), resId));

        container.addView(textView, generateCenterParams());
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAction();
            }
        });
        actionTextView = textView;
        containerView = container;
    }

    public TextView getActionTextView() {
        return actionTextView;
    }

    public View getContainerView() {
        return containerView;
    }

    public String getActionText(Context context) {
        return null;
    }

    protected void onAction() {
        // Empty.
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

}
