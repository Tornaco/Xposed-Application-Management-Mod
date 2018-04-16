package github.tornaco.apigen.service.github;

import java.util.List;

import github.tornaco.apigen.service.github.bean.Contributor;
import github.tornaco.apigen.service.github.bean.GitLog;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by guohao4 on 2017/9/19.
 * Email: Tornaco@163.com
 */

public class GitHubService {

    // https://api.github.com/
    // https://api.github.com/repos/Tornaco/SB
    // https://api.github.com/repos/Tornaco/SB/commits{/sha}
    // https://api.github.com/repos/Tornaco/X-APM/contributors
    private static final String API_URL = "https://api.github.com";


    public interface GitHub {
        @GET("/repos/{owner}/{repo}/commits")
        Call<List<GitLog>> commits(
                @Path("owner") String owner,
                @Path("repo") String repo);

        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo);

        class Factory {
            private static GitHub sHub;

            public synchronized static GitHub create() {
                if (sHub == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(API_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    sHub = retrofit.create(GitHub.class);
                }
                return sHub;
            }
        }
    }

}
