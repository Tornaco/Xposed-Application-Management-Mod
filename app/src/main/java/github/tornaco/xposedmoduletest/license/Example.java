package github.tornaco.xposedmoduletest.license;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.newstand.logger.Logger;

import java.util.ArrayList;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class Example {

    public static void run() {
        License license = new License();
        license.setActiveDate(System.currentTimeMillis());
        license.setExpireDate(System.currentTimeMillis() * 2);
        license.setEmail("tornaco@163.com");
        license.setSource("PUBLIC-SOURCE");

        ArrayList<License> licenses = new ArrayList<>();
        licenses.add(license);

        String js = new Gson().toJson(licenses);
        Logger.d(js);

        licenses = new Gson().fromJson(js, new TypeToken<ArrayList<License>>() {
        }.getType());

        Logger.d(licenses.get(0));
    }
}
