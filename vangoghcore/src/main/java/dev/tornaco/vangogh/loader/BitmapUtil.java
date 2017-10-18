package dev.tornaco.vangogh.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class BitmapUtil {

    private static final int UNCONSTRAINED = -1;
    /* Maximum pixels size for created bitmap. */
    private static final int MAX_NUM_PIXELS_THUMBNAIL = 512 * 512;

    public static String compress(Context context, Bitmap bitmap) throws FileNotFoundException {
        File to = new File(context.getCacheDir().getPath() + File.separator
                + UUID.randomUUID().toString());
        if (!to.getParentFile().exists() && !to.getParentFile().mkdirs()) {
            Logger.e("Fail mkdirs");
            return null;
        }
        if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(to))) {
            return to.getPath();
        }
        return null;
    }

    @Nullable
    public static Bitmap decodeFile(Context context, String path) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeSampleSize(options, MAX_NUM_PIXELS_THUMBNAIL, MAX_NUM_PIXELS_THUMBNAIL);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    @Nullable
    public static Bitmap decodeUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor openFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = null;
        if (openFileDescriptor != null) {
            fileDescriptor = openFileDescriptor.getFileDescriptor();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            options.inSampleSize = computeSampleSize(options, MAX_NUM_PIXELS_THUMBNAIL, MAX_NUM_PIXELS_THUMBNAIL);
            options.inJustDecodeBounds = false;
            Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            openFileDescriptor.close();
            return decodeFileDescriptor;
        }
        return null;
    }

    /*
    * Compute the sample size as a function of minSideLength
    * and maxNumOfPixels.
    * minSideLength is used to specify that minimal width or height of a
    * bitmap.
    * maxNumOfPixels is used to specify the maximal size in pixels that is
    * tolerable in terms of memory usage.
    *
    * The function returns a sample size based on the constraints.
    * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
    * which indicates no care of the corresponding constraint.
    * The functions prefers returning a sample size that
    * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
    *
    * Also, the function rounds up the sample size to a power of 2 or multiple
    * of 8 because BitmapFactory only honors sample size this way.
    * For example, BitmapFactory downsamples an image by 2 even though the
    * request is 3. So we round up the sample size to avoid OOM.
    */
    static int computeSampleSize(BitmapFactory.Options options,
                                 int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    static int computeInitialSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
