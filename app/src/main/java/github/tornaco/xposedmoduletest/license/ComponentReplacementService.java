package github.tornaco.xposedmoduletest.license;

import github.tornaco.xposedmoduletest.bean.ComponentReplacementList;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public interface ComponentReplacementService {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/Tor-Data/master/";

    @GET("app_guard_config")
    Call<ComponentReplacementList> get();

    class Factory {

        private static ComponentReplacementService sHub;

        public synchronized static ComponentReplacementService create() {
            if (sHub == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                sHub = retrofit.create(ComponentReplacementService.class);
            }
            return sHub;
        }
    }
}
