package dev.tornaco.vangogh.media;

import android.content.Context;
import android.support.annotation.DrawableRes;

import dev.tornaco.vangogh.VangoghConfig;
import dev.tornaco.vangogh.display.ImageEffect;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */
@ToString
@Getter
@Setter
public class ImageSource implements Cloneable {
    private Context context;
    private VangoghConfig config;

    private String url;
    @DrawableRes
    private int placeHolder = -1;//DEFAULT SHOW NOTHING.
    @DrawableRes
    private int fallback;

    private ImageEffect[] effect;

    private boolean skipDiskCache;
    private boolean skipMemoryCache;

    public ImageSource duplicate() throws CloneNotSupportedException {
        return (ImageSource) clone();
    }

    private String getEffectClassNames() {
        if (effect == null) return "";
        String str = "";
        for (ImageEffect e : effect) {
            str += e.getClass().getName();
        }
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageSource source = (ImageSource) o;

        boolean urlEquals = url.equals(source.url);
        if (!urlEquals) return false;

        return getEffectClassNames().equals(source.getEffectClassNames());
    }

    public void recycle() {
        config = null;
        context = null;
        url = null;
    }

    @Override
    public int hashCode() {
        int effectCode = 0;
        if (effect != null) {
            for (ImageEffect e : effect) {
                effectCode += e.getClass().getName().hashCode();
            }
        }
        return url.hashCode() + placeHolder + fallback + effectCode;
    }

    public enum SourceType {

        File("file://"),
        Content("content://"),
        Http("http://"),
        Https("https://"),
        Drawable("drawable://"),
        Mipmap("mipmap://"),
        Assets("assets://");

        public String prefix;

        SourceType(String prefix) {
            this.prefix = prefix;
        }
    }

}
