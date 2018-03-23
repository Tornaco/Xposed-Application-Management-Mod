package github.tornaco.xposedmoduletest.remote;

import github.tornaco.xposedmoduletest.model.RemoteConfig;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public interface RemoteConfigService {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/Tor-Data/master/";

    @GET("app_guard_config")
    Call<RemoteConfig> get();

    class Factory {

        private static RemoteConfigService sHub;

        public synchronized static RemoteConfigService create() {
            if (sHub == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                sHub = retrofit.create(RemoteConfigService.class);
            }
            return sHub;
        }
    }
}
