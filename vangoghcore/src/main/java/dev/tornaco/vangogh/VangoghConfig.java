package dev.tornaco.vangogh;

import android.content.Context;

import java.io.File;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/8/27.
 * Email: Tornaco@163.com
 */
@Builder
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VangoghConfig {

    private Context context;

    private File diskCacheDir;
    private int requestPoolSize;
    private int memCachePoolSize;

    static VangoghConfig defaultConfig(Context context) {
        return VangoghConfig
                .builder()
                .context(context)
                .diskCacheDir(new File(context.getCacheDir().getPath() + File.separator + "disk_cache"))
                .memCachePoolSize(64)
                .requestPoolSize(Runtime.getRuntime().availableProcessors() / 4)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VangoghConfig that = (VangoghConfig) o;

        if (requestPoolSize != that.requestPoolSize) return false;
        if (memCachePoolSize != that.memCachePoolSize) return false;
        return diskCacheDir.equals(that.diskCacheDir);

    }

    @Override
    public int hashCode() {
        int result = diskCacheDir.hashCode();
        result = 31 * result + requestPoolSize;
        result = 31 * result + memCachePoolSize;
        return result;
    }
}