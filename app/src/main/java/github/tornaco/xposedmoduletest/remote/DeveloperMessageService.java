package github.tornaco.xposedmoduletest.remote;

import java.util.List;

import github.tornaco.xposedmoduletest.model.PushMessage;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public interface DeveloperMessageService {

    String API_URL = "https://raw.githubusercontent.com/Tornaco/X-APM/master/remote/";

    @GET("developer_messages")
    Call<List<PushMessage>> all();

    class Factory {
        private static DeveloperMessageService sHub;

        public synchronized static DeveloperMessageService create() {
            if (sHub == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                sHub = retrofit.create(DeveloperMessageService.class);
            }
            return sHub;
        }
    }
}
