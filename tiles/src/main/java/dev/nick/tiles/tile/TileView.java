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
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.nick.tiles.R;

public class TileView extends FrameLayout implements View.OnClickListener {

    private static final int DEFAULT_COL_SPAN = 1;

    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mStatusTextView;
    private View mDivider;

    private int mColSpan = DEFAULT_COL_SPAN;

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

    protected void onCreate(Context context) {
    }

    protected void onViewInflated(View view) {
        mImageView = (ImageView) view.findViewById(R.id.icon);
        if (useStaticTintColor())
            mImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.tile_icon_tint));
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mStatusTextView = (TextView) view.findViewById(R.id.status);
        mDivider = view.findViewById(R.id.tile_divider);

        onBindActionView((RelativeLayout) view.findViewById(R.id.action_area));

        setOnClickListener(this);

        setBackgroundResource(R.drawable.dashboard_tile_background);
        setFocusable(true);
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

    protected boolean useStaticTintColor() {
        return true;
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
}
