package github.tornaco.xposedmoduletest.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Tornaco on 2018/3/23 11:57.
 * God bless no bug!
 */

public class XInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "XInstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, XRegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]
}
