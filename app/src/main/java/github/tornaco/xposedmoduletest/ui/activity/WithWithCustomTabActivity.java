package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by guohao4 on 2017/9/22.
 * Email: Tornaco@163.com
 */

public abstract class WithWithCustomTabActivity extends BaseActivity {

    public void navigateToWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
