/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.tiles.tile;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.nick.tiles.R;

public class TileView extends FrameLayout
        implements View.OnClickListener,
        View.OnLongClickListener {

    private static final int DEFAULT_COL_SPAN = 1;

    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mStatusTextView;
    private View mDivider;

    private int mColSpan = DEFAULT_COL_SPAN;

    private boolean mEnabledAndroidPStyledIcon = true;

    public TileView(Context context) {
        this(context, null);
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
        final View view = LayoutInflater.from(context).inflate(getLayoutId(), this);
        onViewInflated(view);
    }

    protected int getLayoutId() {
        return R.layout.dashboard_tile;
    }

    @CallSuper
    protected void onCreate(Context context) {
        mEnabledAndroidPStyledIcon = context.getResources().getBoolean(R.bool.dashboard_android_p_icon);
    }

    protected void onViewInflated(View view) {
        mImageView = view.findViewById(R.id.icon);

        applyImageViewStyle();

        mTitleTextView = view.findViewById(R.id.title);
        mStatusTextView = view.findViewById(R.id.status);
        mDivider = view.findViewById(R.id.tile_divider);

        onBindActionView((RelativeLayout) view.findViewById(R.id.action_area));

        setOnClickListener(this);
        setOnLongClickListener(this);

        setBackgroundResource(R.drawable.dashboard_tile_background);
        setFocusable(true);
    }

    private void applyImageViewStyle() {
        if (mImageView != null) {
            if (mEnabledAndroidPStyledIcon && getImageViewBackgroundRes() != 0) {
                mImageView.setBackgroundResource(getImageViewBackgroundRes());
                mImageView.setColorFilter(Color.WHITE);
            } else {
                mImageView.setBackgroundResource(0);
                mImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.tile_icon_tint));
                mImageView.setPadding(0, 0, 0, 0);// Remove padding.
                // Make the icon smaller.
                mImageView.setScaleX(0.8f);
                mImageView.setScaleY(0.8f);
            }
        }
    }

    @DrawableRes
    protected int getImageViewBackgroundRes() {
        return mEnabledAndroidPStyledIcon ? R.drawable.tile_bg_grey_dark : 0;
    }

    protected void onBindActionView(RelativeLayout container) {
    }

    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public TextView getSummaryTextView() {
        return mStatusTextView;
    }

    public ImageView getImageView() {
        return mImageView;
    }


    public void setDividerVisibility(boolean visible) {
        mDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    int getColumnSpan() {
        return mColSpan;
    }

    void setColumnSpan(int span) {
        mColSpan = span;
    }

    public void setEnabledAndroidPStyledIcon(boolean enable) {
        if (enable != this.mEnabledAndroidPStyledIcon) {
            this.mEnabledAndroidPStyledIcon = enable;
            applyImageViewStyle();
        }
    }

    @Override
    public void onClick(View v) {

    }

    protected RelativeLayout.LayoutParams generateCenterParams() {
        return generateCenterParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    protected RelativeLayout.LayoutParams generateCenterParams(int w, int h) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
