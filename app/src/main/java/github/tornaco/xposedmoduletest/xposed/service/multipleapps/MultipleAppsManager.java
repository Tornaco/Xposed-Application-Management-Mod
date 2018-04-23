package github.tornaco.xposedmoduletest.xposed.service.multipleapps;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;

import java.util.List;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/4/23 10:48.
 * God bless no bug!
 */
public class MultipleAppsManager {

    private static final String MULTIPLE_APPS_USER_NAME = "x-apm-multiple_apps";

    @Getter
    private Context context;

    public MultipleAppsManager(Context context) {
        this.context = context;
    }

    public void onStart() {
        // Create user if need.
        UserManager userManager = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
        if (userManager != null) {

            boolean multipleAppsUserExist = false;

            List<UserHandle> userHandles = userManager.getUserProfiles();
            for (UserHandle uh : userHandles) {
                UserInfo ui = userManager.getUserInfo(uh.getIdentifier());
                XposedLog.boot("MultipleAppsManager, onStart, uh: " + uh.toString() + ", ui: " + ui);
                if (MULTIPLE_APPS_USER_NAME.equals(ui.name)) {
                    multipleAppsUserExist = true;
                }
            }

            if (!multipleAppsUserExist) {
                // Let's go.
                UserInfo ui = userManager.createUser(MULTIPLE_APPS_USER_NAME, UserInfo.FLAG_MANAGED_PROFILE);
                XposedLog.boot("MultipleAppsManager, onStart, create user: " + ui);
            }
        }

    }
}
