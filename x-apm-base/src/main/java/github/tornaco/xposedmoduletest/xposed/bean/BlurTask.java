package github.tornaco.xposedmoduletest.xposed.bean;

import android.graphics.Bitmap;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/5/9 16:30.
 * God bless no bug!
 */
@AllArgsConstructor
@ToString
public class BlurTask {

    public String packageName;
    public long updateTimeMills;
    public Bitmap bitmap;
    public Object obj;

    public static BlurTask from(String pkg, Bitmap bitmap) {
        return new BlurTask(pkg, System.currentTimeMillis(), bitmap, null);
    }

    public static BlurTask from(String pkg, Bitmap bitmap, Object obj) {
        return new BlurTask(pkg, System.currentTimeMillis(), bitmap, obj);
    }
}
