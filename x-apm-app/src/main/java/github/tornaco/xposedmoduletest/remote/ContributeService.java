package github.tornaco.xposedmoduletest.remote;

import java.util.List;

import github.tornaco.xposedmoduletest.model.Contribution;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public interface ContributeService {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/Tor-Data/master/";

    @GET("pay_list_app_guard")
    Call<List<Contribution>> all();

    class Factory {
        private static ContributeService sHub;

        public synchronized static ContributeService create() {
            if (sHub == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                sHub = retrofit.create(ContributeService.class);
            }
            return sHub;
        }
    }
}
