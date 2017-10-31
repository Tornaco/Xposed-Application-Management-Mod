package github.tornaco.xposedmoduletest.x.submodules;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

abstract class AndroidSubModuleModule extends AbsSubModule {
    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("android");
    }
}
