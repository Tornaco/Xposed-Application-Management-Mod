package github.tornaco.xposedmoduletest.loader;

import android.text.TextUtils;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.PayExtra;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;

/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */

public class PayExtraLoader {

    public interface Callback {
        void onError(Throwable e);

        void onSuccess(List<PayExtra> extras);
    }

    public void loadAsync(final String from, final Callback callback) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                load(from, callback);
            }
        });
    }

    public void load(String from, final Callback callback) {
        String tmpDir = Files.createTempDir().getPath();
        final String fileName = tmpDir + File.separator + "pays";

        AsyncHttpClient.getDefaultInstance().executeFile(new AsyncHttpGet(from),
                fileName,
                new AsyncHttpClient.FileCallback() {
                    @Override
                    public void onCompleted(Exception e, AsyncHttpResponse source, File result) {
                        Logger.i("onCompleted %s, %s", e, result);
                        if (e == null) {

                            String content = FileUtil.readString(result.getPath());
                            if (TextUtils.isEmpty(content)) {
                                callback.onError(new Exception("Empty content"));
                                return;
                            }

                            try {
                                final ArrayList<PayExtra> payExtras = new Gson().fromJson(content,
                                        new TypeToken<ArrayList<PayExtra>>() {
                                        }.getType());
                                callback.onSuccess(payExtras);
                            } catch (Throwable w) {
                                Logger.e("Fail to json %s", Logger.getStackTraceString(w));
                                callback.onError(w);
                            }


                        } else {
                            callback.onError(e);
                        }
                    }
                });

    }
}
