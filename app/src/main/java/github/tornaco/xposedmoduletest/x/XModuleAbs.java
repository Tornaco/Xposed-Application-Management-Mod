package github.tornaco.xposedmoduletest.x;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;

/**
 * Created by guohao4 on 2017/10/28.
 * Email: Tornaco@163.com
 */

abstract class XModuleAbs implements IXposedHookLoadPackage, IXposedHookZygoteInit,
        IXposedHookInitPackageResources {
}
