package dev.tornaco.vangogh.media;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */

public interface ImageDecoder {
    @NonNull
    Image decodeStream(@NonNull InputStream stream) throws IOException;
}
