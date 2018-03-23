package github.tornaco.xposedmoduletest.remote;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.model.Contribution;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class Contributions {

    public interface Callback {
        void onError(Throwable e);

        void onSuccess(List<Contribution> contributions);
    }

    public static void loadAsync(final String from, final Callback callback) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                load(callback);
            }
        });
    }

    public static void load(Callback callback) {
        ContributeService contributeService = ContributeService.Factory.create();
        try {
            List<Contribution> licenseList = contributeService.all().execute().body();
            if (licenseList == null) licenseList = Lists.newArrayListWithCapacity(0);// Avoid npe.
            callback.onSuccess(licenseList);
        } catch (Exception e) {
            Logger.e("Contributions reload error: " + e);
            callback.onError(e);
        }
    }
}
