package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;

import github.tornaco.xposedmoduletest.IAshmanService;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
abstract class XAshmanServiceAbs extends IAshmanService.Stub
        implements IModuleBridge {

    @Getter
    @Setter
    private Context context;

    @Override
    public void attachContext(Context context) {
        setContext(context);
    }
}
