package github.tornaco.xposedmoduletest.compat.os;

import android.content.Context;

import github.tornaco.xposedmoduletest.R;
import lombok.Synchronized;

/**
 * Created by Tornaco on 2018/5/16 12:12.
 * God bless no bug!
 */
public class XAppOpsManagerRes {

    // Map op to op icon res.
    // Should only be retrieved on app.
    private static int[] sOpToIcon = new int[]{
            R.drawable.ic_location_on_black_24dp, // OP_COARSE_LOCATION,
            R.drawable.ic_location_on_black_24dp, // OP_COARSE_LOCATION,
            R.drawable.ic_location_on_black_24dp,
            R.drawable.ic_vibration_black_24dp, // OP_VIBRATE,
            R.drawable.ic_account_circle_black_24dp,
            R.drawable.ic_account_circle_black_24dp,
            R.drawable.ic_call_black_24dp,
            R.drawable.ic_call_black_24dp,
            R.drawable.ic_perm_contact_calendar_black_24dp,
            R.drawable.ic_perm_contact_calendar_black_24dp,
            R.drawable.ic_location_on_black_24dp,
            R.drawable.ic_notifications_black_24dp,// OP_POST_NOTIFICATION,
            R.drawable.ic_location_on_black_24dp, // OP_COARSE_LOCATION,
            R.drawable.ic_call_black_24dp,
            R.drawable.ic_sms_black_24dp, // OP_READ_SMS,
            R.drawable.ic_sms_black_24dp, // OP_WRITE_SMS,
            R.drawable.ic_sms_black_24dp, //OP_RECEIVE_SMS,
            R.drawable.ic_sms_black_24dp, // OP_RECEIVE_SMS,
            R.drawable.ic_mms_black_24dp, // OP_RECEIVE_MMS,
            R.drawable.ic_mms_black_24dp, //OP_RECEIVE_WAP_PUSH,
            R.drawable.ic_sms_black_24dp, // P_SEND_SMS,
            R.drawable.ic_sms_black_24dp, // OP_READ_SMS,
            R.drawable.ic_sms_black_24dp, // OP_WRITE_SMS,
            R.drawable.ic_settings_black_24dp, // OP_WRITE_SETTINGS,
            R.drawable.ic_add_alert_black_24dp, // OP_SYSTEM_ALERT_WINDOW,
            R.drawable.ic_notifications_black_24dp, // OP_ACCESS_NOTIFICATIONS,
            R.drawable.ic_camera_black_24dp, // OP_CAMERA,
            R.drawable.ic_fiber_smart_record_black_24dp, //OP_RECORD_AUDIO,
            R.drawable.ic_audiotrack_black_24dp, // OP_PLAY_AUDIO,
            R.drawable.ic_content_paste_black_24dp, // OP_READ_CLIPBOARD,
            R.drawable.ic_content_paste_black_24dp, // OP_WRITE_CLIPBOARD,
            R.drawable.ic_perm_media_black_24dp, // OP_TAKE_MEDIA_BUTTONS,
            R.drawable.ic_perm_media_black_24dp, //    OP_TAKE_AUDIO_FOCUS,
            R.drawable.ic_volume_down_black_24dp, //   OP_AUDIO_MASTER_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //   OP_AUDIO_VOICE_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //  OP_AUDIO_RING_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //  OP_AUDIO_MEDIA_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //  OP_AUDIO_ALARM_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //  OP_AUDIO_NOTIFICATION_VOLUME,
            R.drawable.ic_volume_down_black_24dp, //  OP_AUDIO_BLUETOOTH_VOLUME,
            R.drawable.ic_memory_black_24dp, //  OP_WAKE_LOCK,
            R.drawable.ic_location_on_black_24dp, //  OP_COARSE_LOCATION,
            R.drawable.ic_location_on_black_24dp, //  OP_COARSE_LOCATION,
            R.drawable.ic_perm_device_information_black_24dp, //  OP_GET_USAGE_STATS,
            R.drawable.ic_mic_off_black_24dp, //    OP_MUTE_MICROPHONE,
            R.drawable.ic_add_alert_black_24dp, //   OP_TOAST_WINDOW,
            R.drawable.ic_cast_black_24dp, //   OP_PROJECT_MEDIA,
            R.drawable.ic_vpn_key_black_24dp, //   OP_ACTIVATE_VPN,
            R.drawable.ic_wallpaper_black_24dp, //  OP_WRITE_WALLPAPER,
            R.drawable.ic_assistant_black_24dp, //  OP_ASSIST_STRUCTURE,
            R.drawable.ic_camera_black_24dp, //   OP_ASSIST_SCREENSHOT,
            R.drawable.ic_call_black_24dp, //   OP_READ_PHONE_STATE,
            R.drawable.ic_voicemail_black_24dp, //   OP_ADD_VOICEMAIL,
            R.drawable.ic_call_black_24dp, //  OP_USE_SIP,
            R.drawable.ic_call_black_24dp, //  OP_PROCESS_OUTGOING_CALLS,
            R.drawable.ic_fingerprint_black_24dp, //   OP_USE_FINGERPRINT,
            R.drawable.ic_directions_run_black_24dp, //   OP_BODY_SENSORS,
            R.drawable.ic_network_cell_black_24dp, //   OP_READ_CELL_BROADCASTS,
            R.drawable.ic_location_on_black_24dp, //   OP_MOCK_LOCATION,
            R.drawable.ic_sd_storage_black_24dp, //  OP_READ_EXTERNAL_STORAGE,
            R.drawable.ic_sd_storage_black_24dp, //   OP_WRITE_EXTERNAL_STORAGE,
            R.drawable.ic_brightness_low_black_24dp, //   OP_TURN_SCREEN_ON,
            R.drawable.ic_account_circle_black_24dp, //  OP_GET_ACCOUNTS,
            R.drawable.ic_adb_black_24dp, //   OP_RUN_IN_BACKGROUND,
            R.drawable.ic_audiotrack_black_24dp, //   OP_AUDIO_ACCESSIBILITY_VOLUME,
            R.drawable.ic_phone_android_black_24dp, //   OP_READ_PHONE_NUMBERS,
            R.drawable.ic_adb_black_24dp, //   OP_REQUEST_INSTALL_PACKAGES,
            R.drawable.ic_view_agenda_black_24dp, //   OP_PICTURE_IN_PICTURE,
            R.drawable.ic_adb_black_24dp, //   OP_INSTANT_APP_START_FOREGROUND,
            R.drawable.ic_call_black_24dp, //   OP_ANSWER_PHONE_CALLS

            // EXTRAS.
            R.drawable.ic_apps_black_24dp, //   OP_READ_INSTALLED_APPS
            R.drawable.ic_android_black_24dp, //   OP_GET_RUNNING_TASKS
            R.drawable.ic_phone_android_black_24dp, //   OP_GET_DEVICE_ID
            R.drawable.ic_sim_card_black_24dp, //   OP_GET_SIM_SERIAL_NUMBER
            R.drawable.ic_sim_card_black_24dp, //   OP_GET_LINE1_NUMBER
            R.drawable.ic_access_alarm_black_24dp, //   ALARM
            R.drawable.ic_room_service_black_24dp, // OP_START_SERVICE
            R.drawable.ic_room_service_black_24dp, // SHELL
            R.drawable.ic_adb_black_24dp, // FC
            R.drawable.ic_brightness_auto_black_24dp, //BRIGHTNESS
    };

    static {
        //noinspection ConstantConditions
        if (sOpToIcon.length != XAppOpsManager._NUM_OP) {
            throw new IllegalStateException("sOpToIcon length " + sOpToIcon.length
                    + " should be " + XAppOpsManager._NUM_OP);
        }
    }

    private final CharSequence[] mOpSummaries;
    private final CharSequence[] mOpLabels;

    private XAppOpsManagerRes(Context context) {
        mOpSummaries = context.getResources().getTextArray(R.array.app_ops_summaries);
        mOpLabels = context.getResources().getTextArray(R.array.app_ops_labels);
    }

    private static XAppOpsManagerRes sMe = null;

    @Synchronized
    public static XAppOpsManagerRes from(Context context) {
        if (sMe == null) sMe = new XAppOpsManagerRes(context);
        return sMe;
    }

    public static String getOpLabel(Context context, int code) {
        if (code == XAppOpsManager.OP_NONE) return "UNKNOWN";

        return String.valueOf(from(context).mOpLabels[code]);
    }

    public static String getOpSummary(Context context, int code) {
        if (code == XAppOpsManager.OP_NONE) return "UNKNOWN";
        return String.valueOf(from(context).mOpSummaries[code]);
    }

    /**
     * Retrieve the permission associated with an operation, or null if there is not one.
     */
    public static int opToIconRes(int op) {
        if (op >= XAppOpsManager._NUM_OP) return R.drawable.ic_account_circle_black_24dp;
        return sOpToIcon[op];
    }
}
