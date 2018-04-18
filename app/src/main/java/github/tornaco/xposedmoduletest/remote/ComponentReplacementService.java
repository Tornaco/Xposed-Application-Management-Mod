package github.tornaco.xposedmoduletest.remote;

import java.util.List;

import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public interface ComponentReplacementService {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/X-APM/master/remote/";

    @GET("component_replacements")
    Call<List<ComponentReplacement>> get();

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
