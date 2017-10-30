package github.tornaco.xposedmoduletest.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;
import com.novoda.simplechromecustomtabs.navigation.IntentCustomizer;
import com.novoda.simplechromecustomtabs.navigation.NavigationFallback;
import com.novoda.simplechromecustomtabs.navigation.SimpleChromeCustomTabsIntentBuilder;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/9/22.
 * Email: Tornaco@163.com
 */

public abstract class WithWithCustomTabActivity extends BaseActivity {

    private final IntentCustomizer intentCustomizer = new IntentCustomizer() {
        @Override
        public SimpleChromeCustomTabsIntentBuilder
        onCustomiseIntent(SimpleChromeCustomTabsIntentBuilder simpleChromeCustomTabsIntentBuilder) {
            return simpleChromeCustomTabsIntentBuilder
                    .withToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent))
                    .showingTitle()
                    .withUrlBarHiding()
                    //.withCloseButtonIcon(decodeCloseBitmap())
                    .withDefaultShareMenuItem();
            //.withActionButton(decodeShareBitmap(), getString(R.string.share), shareUrl(), false)
            //.withMenuItem(getString(R.string.view_demo_source_code), viewSourceCode())
            //.withExitAnimations(getApplicationContext(), android.R.anim.slide_in_left, android.R.anim.fade_out)
            //.withStartAnimations(getApplicationContext(), android.R.anim.fade_in, android.R.anim.slide_out_right);
        }
    };

    public void navigateToWebPage(String url) {
        Uri uri = Uri.parse(url);
        NavigationFallback fallback = new NavigationFallback() {
            @Override
            public void onFallbackNavigateTo(Uri url) {
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(url)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        SimpleChromeCustomTabs.getInstance().withFallback(fallback)
                .withIntentCustomizer(intentCustomizer)
                .navigateTo(uri, this);
    }


    @Override
    public void onResume() {
        super.onResume();
        SimpleChromeCustomTabs.getInstance().connectTo(this);
    }

    @Override
    public void onPause() {
        SimpleChromeCustomTabs.getInstance().disconnectFrom(this);
        super.onPause();
    }
}
