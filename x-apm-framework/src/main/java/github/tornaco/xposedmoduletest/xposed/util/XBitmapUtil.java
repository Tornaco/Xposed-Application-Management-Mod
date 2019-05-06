/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package github.tornaco.xposedmoduletest.xposed.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import github.tornaco.xposedmoduletest.xposed.DefaultConfiguration;

/**
 * {@link Bitmap} specific helpers.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class XBitmapUtil {

    public static final float BITMAP_SCALE = DefaultConfiguration.BITMAP_SCALE;
    /* Initial blur radius. */
    public static final int BLUR_RADIUS = DefaultConfiguration.BLUR_RADIUS;
    public static final int BLUR_RADIUS_MAX = DefaultConfiguration.BLUR_RADIUS_MAX;

    private static RenderScript sRs;

    /**
     * This class is never instantiated
     */
    private XBitmapUtil() {
    }

    public static Bitmap createHWBitmap(final Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Takes a bitmap and creates a new slightly blurry version of it.
     *
     * @param sentBitmap The {@link Bitmap} to blur.
     * @return A blurred version of the given {@link Bitmap}.
     */
    public static Bitmap createBlurredBitmap(final Bitmap sentBitmap) {
        return createBlurredBitmap(sentBitmap, BLUR_RADIUS, BITMAP_SCALE);
    }


    /**
     * Takes a bitmap and creates a new slightly blurry version of it.
     *
     * @param sentBitmap The {@link Bitmap} to blur.
     * @param radius     Radius of the blur effect.
     * @return A blurred version of the given {@link Bitmap}.
     */
    public static Bitmap createBlurredBitmap(final Bitmap sentBitmap, int radius, float scale) {
        if (sentBitmap == null) {
            return null;
        }

        // Stack Blur v1.0 init
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);
        final Bitmap mBitmap = Bitmap.createBitmap(inputBitmap);

        final int w = mBitmap.getWidth();
        final int h = mBitmap.getHeight();

        final int[] pix = new int[w * h];
        mBitmap.getPixels(pix, 0, w, 0, 0, w, h);

        final int wm = w - 1;
        final int hm = h - 1;
        final int wh = w * h;
        final int div = radius + radius + 1;

        final int r[] = new int[wh];
        final int g[] = new int[wh];
        final int b[] = new int[wh];
        final int vmin[] = new int[Math.max(w, h)];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;

        int divsum = div + 1 >> 1;
        divsum *= divsum;
        final int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = i / divsum;
        }

        yw = yi = 0;

        final int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        final int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = 0xff000000 | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        mBitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return mBitmap;
    }

    public synchronized static Bitmap rsBlur(Context context, Bitmap inputBitmap, int radius) {
        if (inputBitmap == null) {
            return null;
        }
        // Define this only once if blurring multiple times
        if (sRs == null) {
            sRs = RenderScript.create(context);
        }

        // This will blur the bitmapOriginal with a radius of 8 and save it in bitmapOriginal
        // use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation input = Allocation.createFromBitmap(sRs, inputBitmap);
        final Allocation output = Allocation.createTyped(sRs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(sRs, Element.U8_4(sRs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(inputBitmap);
        return inputBitmap;
    }
}