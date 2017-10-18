package github.tornaco.xposedmoduletest.bean;

import android.content.Context;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import de.greenrobot.dao.identityscope.IdentityScopeType;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public enum DaoManager {

    Instance;

    private DaoSession session;

    public static DaoManager getInstance() {
        return Instance;
    }

    private void init(Context context) {
        try {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "guard_db", null);
            DaoMaster daoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
            session = daoMaster.newSession(IdentityScopeType.None);
        } catch (Throwable e) {
            Logger.e("Fail init session:" + Logger.getStackTraceString(e));
        }
    }

    public
    @Nullable
    synchronized DaoSession getSession(Context context) {
        if (session == null) init(context);
        return session;
    }
}
