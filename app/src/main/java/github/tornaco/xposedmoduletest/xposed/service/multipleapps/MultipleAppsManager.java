package github.tornaco.xposedmoduletest.xposed.service.multipleapps;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;

import java.util.List;

import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/4/23 10:48.
 * God bless no bug!
 */
public class MultipleAppsManager {

    public static final int FLAG_MULTIPLE_APPS = 0x00000400;

    private static final Singleton<MultipleAppsManager>
            sMe = new Singleton<MultipleAppsManager>() {
        @Override
        protected MultipleAppsManager create() {
            return new MultipleAppsManager();
        }
    };

    public static final String MULTIPLE_APPS_USER_NAME = "x-apm-multiple_apps";

    public static MultipleAppsManager getInstance() {
        return sMe.get();
    }

    private int mMultipleAppsUserId;

    @Getter
    private Context context;

    public void onCreate(Context context) {
        this.context = context;
    }

    private MultipleAppsManager() {

    }

    public void createMultipleProfileIfNeed() {
        // Create user if need.
        UserManager userManager = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
        if (userManager != null) {

            boolean multipleAppsUserExist = false;

            List<UserInfo> userInfoList = userManager.getUsers();

            for (UserInfo userInfo : userInfoList) {
                XposedLog.boot("MultipleAppsManager, createMultipleProfileIfNeed  ui: " + userInfo);

                if (MULTIPLE_APPS_USER_NAME.equals(userInfo.name)) {
                    multipleAppsUserExist = true;
                    mMultipleAppsUserId = userInfo.id;
                    XposedLog.boot("MultipleAppsManager, createMultipleProfileIfNeed exist, mMultipleAppsUserId: " + mMultipleAppsUserId);
                }
            }

            //noinspection ConstantConditions
            if (!multipleAppsUserExist) {
                // Let's go.
                UserInfo ui = userManager.createUser(MULTIPLE_APPS_USER_NAME, UserInfo.FLAG_INITIALIZED);
                XposedLog.boot("MultipleAppsManager, createMultipleProfileIfNeed, create user: " + ui);
                mMultipleAppsUserId = ui.id;
            }

            // Start user.
            startMultipleAppsUserInBackground();
        }
    }

    private void startMultipleAppsUserInBackground() {
        try {
            boolean res = ActivityManagerNative.getDefault().startUserInBackground(mMultipleAppsUserId);
            XposedLog.boot("MultipleAppsManager, startUserInBackground, res: " + res);
        } catch (RemoteException e) {
            XposedLog.wtf("Fail startUserInBackground: " + Log.getStackTraceString(e));
        }
    }

    public boolean installAppToMultipleAppsUser(String pkgName) {
        createMultipleProfileIfNeed();
        XposedLog.boot("MultipleAppsManager, installAppToMultipleAppsUser, mMultipleAppsUserId: " + mMultipleAppsUserId);
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.installExistingPackageAsUser(pkgName, mMultipleAppsUserId);
            XposedLog.boot("MultipleAppsManager, installAppToMultipleAppsUser, Success");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            XposedLog.wtf("Fail installApp: " + Log.getStackTraceString(e));
            return false;
        }
    }

    public int getMultipleAppsUserId() {
        return mMultipleAppsUserId;
    }
}
