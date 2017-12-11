package github.tornaco.xposedmoduletest.xposed.submodules;

import java.util.HashSet;
import java.util.Set;

import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class IntentFirewallSubModuleManager {

    private static IntentFirewallSubModuleManager sMe;

    private final Set<SubModule> SUBS = new HashSet<>();

    private IntentFirewallSubModuleManager() {
        // SUBS.add(new NetworkManagementModule());
        SUBS.add(new MediaFocusControlSubModule2());
        SUBS.add(new MediaFocusControlSubModule());
        SUBS.add(new PMSSubModule2());
        SUBS.add(new ASDSubModule());
        SUBS.add(new IFWSubModule());
        SUBS.add(new AMSSubModule6());
        SUBS.add(new AMSSubModule5());
        SUBS.add(new AMSSubModule4());
        SUBS.add(new AMSSubModule3());
        SUBS.add(new AMSSubModule2());
        SUBS.add(new AMSSubModule());
    }

    @Synchronized
    public
    static IntentFirewallSubModuleManager getInstance() {
        if (sMe == null) sMe = new IntentFirewallSubModuleManager();
        return sMe;
    }

    public Set<SubModule> getAllSubModules() {
        return SUBS;
    }
}
