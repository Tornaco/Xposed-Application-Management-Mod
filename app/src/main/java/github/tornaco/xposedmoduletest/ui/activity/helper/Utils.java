/**
 * Copyright (C) 2007 Google Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceFrameLayout;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabWidget;

import github.tornaco.xposedmoduletest.R;

public final class Utils {

    /**
     * Prepare a custom preferences layout, moving padding to {@link ListView}
     * when outside scrollbars are requested. Usually used to display
     * {@link ListView} and {@link TabWidget} with correct padding.
     */
    public static void prepareCustomPreferencesList(
            ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        final boolean movePadding = list.getScrollBarStyle() == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (movePadding) {
            final Resources res = list.getResources();
            final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
            final int paddingBottom = res.getDimensionPixelSize(
                    com.android.internal.R.dimen.preference_fragment_padding_bottom);

            if (parent instanceof PreferenceFrameLayout) {
                ((PreferenceFrameLayout.LayoutParams) child.getLayoutParams()).removeBorders = true;

                final int effectivePaddingSide = ignoreSidePadding ? 0 : paddingSide;
                list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
            } else {
                list.setPaddingRelative(paddingSide, 0, paddingSide, paddingBottom);
            }
        }
    }

    @ColorInt
    public static int getColorAccent(Context context) {
        return getColorAttr(context, android.R.attr.colorAccent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ColorInt
    public static int getDefaultColor(Context context, int resId) {
        final ColorStateList list =
                context.getResources().getColorStateList(resId, context.getTheme());

        return list.getDefaultColor();
    }

    @ColorInt
    public static int getDisabled(Context context, int inputColor) {
        return applyAlphaAttr(context, android.R.attr.disabledAlpha, inputColor);
    }

    @ColorInt
    public static int applyAlphaAttr(Context context, int attr, int inputColor) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float alpha = ta.getFloat(0, 0);
        ta.recycle();
        return applyAlpha(alpha, inputColor);
    }

    @ColorInt
    public static int applyAlpha(float alpha, int inputColor) {
        alpha *= Color.alpha(inputColor);
        return Color.argb((int) (alpha), Color.red(inputColor), Color.green(inputColor),
                Color.blue(inputColor));
    }

    @ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static int getThemeAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int theme = ta.getResourceId(0, 0);
        ta.recycle();
        return theme;
    }

}
