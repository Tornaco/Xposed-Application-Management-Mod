package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
abstract class XAppGuardServiceAbs {

    @Getter
    @Setter
    private Context context;

    public void attachContext(Context context) {
        setContext(context);
    }
}
