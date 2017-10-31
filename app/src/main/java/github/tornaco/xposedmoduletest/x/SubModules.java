package github.tornaco.xposedmoduletest.x;

import java.util.Set;

/**
 * Created by guohao4 on 2017/10/30.
 * Email: Tornaco@163.com
 */

public interface SubModules {

    void onAppGuardServicePublished(XAppGuardServiceAbs service);

    Set<String> getInterestedPackages();

    SubModuleStatus getStatus();

    String getErrorMessage();

    String name();


    enum SubModuleStatus {
        ERROR,
        READY
    }
}
