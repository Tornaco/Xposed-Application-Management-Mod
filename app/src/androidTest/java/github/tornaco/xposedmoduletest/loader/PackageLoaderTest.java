package github.tornaco.xposedmoduletest.loader;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;
import org.newstand.logger.Logger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.PackageInfo;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */
public class PackageLoaderTest {
    @Test
    public void loadInstalled() throws Exception {
        Collections.consumeRemaining(PackageLoader.Impl.create(InstrumentationRegistry.getTargetContext())
                .loadInstalled(false), new Consumer<PackageInfo>() {
            @Override
            public void accept(PackageInfo packageInfo) {
                Logger.d("PackageInfo:" + packageInfo.getPkgName());

                long id = DaoManager.getInstance().getSession(InstrumentationRegistry.getTargetContext())
                        .getPackageInfoDao().insert(packageInfo);
                Logger.d(id);
            }
        });
    }

    @Test
    public void loadStored() throws Exception {
        Collections.consumeRemaining(PackageLoader.Impl.create(InstrumentationRegistry.getTargetContext())
                .loadStored(), new Consumer<PackageInfo>() {
            @Override
            public void accept(PackageInfo packageInfo) {
                Logger.d("PackageInfo-Store:" + packageInfo.getPkgName());
            }
        });
    }

}