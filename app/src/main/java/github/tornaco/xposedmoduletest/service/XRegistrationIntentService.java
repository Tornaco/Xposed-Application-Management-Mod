package github.tornaco.xposedmoduletest.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.newstand.logger.Logger;

import java.io.IOException;

import dev.nick.eventbus.EventBus;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.xposed.XApp;

/**
 * Created by Tornaco on 2018/3/23 11:58.
 * God bless no bug!
 */

public class XRegistrationIntentService extends IntentService {

    private static final String TAG = "XRegistrationService";
    private static final String[] TOPICS = {"global"};

    public XRegistrationIntentService() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public XRegistrationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Logger.d("GCM Registration onHandleIntent: " + intent);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Logger.d("GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            AppSettings.setSentTokenToServer(this, true);
            // [END register_for_gcm]
        } catch (IOException e) {
            Logger.e("Failed to complete token refresh: " + e);
            AppSettings.setSentTokenToServer(this, false);
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        EventBus.from().publishEmptyEvent(XApp.EVENT_GCM_REGISTRATION_COMPLETE);
    }

    /**
     * Persist registration to third-party servers.
     * <p>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
