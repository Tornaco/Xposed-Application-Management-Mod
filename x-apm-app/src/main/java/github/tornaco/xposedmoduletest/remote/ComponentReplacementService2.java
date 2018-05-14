package github.tornaco.xposedmoduletest.remote;

import java.util.List;

import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Tornaco on 2018/3/27 15:00.
 * God bless no bug!
 */

public interface ComponentReplacementService2 {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/XAppGuard/master/remote/";

    @GET("component_replacements")
    Call<List<ComponentReplacement>> get();

    class Factory {

        private static ComponentReplacementService2 sHub;

        public synchronized static ComponentReplacementService2 create() {
            if (sHub == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                sHub = retrofit.create(ComponentReplacementService2.class);
            }
            return sHub;
        }
    }
}
