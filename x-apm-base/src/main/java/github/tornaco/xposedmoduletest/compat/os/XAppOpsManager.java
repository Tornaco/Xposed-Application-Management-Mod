/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.tornaco.xposedmoduletest.compat.os;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Set;

import lombok.Synchronized;

/**
 * Helper for accessing features in {@link AppOpsManager}.
 */
public final class XAppOpsManager {

    public static final int MODE_ALLOWED = 0;

    public static final int MODE_IGNORED = 1;

    public static final int MODE_ERRORED = 2;

    public static final int MODE_DEFAULT = 3;


    // when adding one of these:
    //  - increment _NUM_OP
    //  - add rows to sOpToSwitch, sOpToString, sOpNames, sOpToPerms, sOpDefault
    //  - add descriptive strings to Settings/res/values/arrays.xml
    //  - add the op to the appropriate template in AppOpsState.OpsTemplate (settings app)

    /**
     * @hide No operation specified.
     */
    public static final int OP_NONE = -1;
    /**
     * @hide Access to coarse location information.
     */
    public static final int OP_COARSE_LOCATION = 0;
    /**
     * @hide Access to fine location information.
     */
    public static final int OP_FINE_LOCATION = 1;
    /**
     * @hide Causing GPS to run.
     */
    public static final int OP_GPS = 2;
    /**
     * @hide
     */
    public static final int OP_VIBRATE = 3;
    /**
     * @hide
     */
    public static final int OP_READ_CONTACTS = 4;
    /**
     * @hide
     */
    public static final int OP_WRITE_CONTACTS = 5;
    /**
     * @hide
     */
    public static final int OP_READ_CALL_LOG = 6;
    /**
     * @hide
     */
    public static final int OP_WRITE_CALL_LOG = 7;
    /**
     * @hide
     */
    public static final int OP_READ_CALENDAR = 8;
    /**
     * @hide
     */
    public static final int OP_WRITE_CALENDAR = 9;
    /**
     * @hide
     */
    public static final int OP_WIFI_SCAN = 10;
    /**
     * @hide
     */
    public static final int OP_POST_NOTIFICATION = 11;
    /**
     * @hide
     */
    public static final int OP_NEIGHBORING_CELLS = 12;
    /**
     * @hide
     */
    public static final int OP_CALL_PHONE = 13;
    /**
     * @hide
     */
    public static final int OP_READ_SMS = 14;
    /**
     * @hide
     */
    public static final int OP_WRITE_SMS = 15;
    /**
     * @hide
     */
    public static final int OP_RECEIVE_SMS = 16;
    /**
     * @hide
     */
    public static final int OP_RECEIVE_EMERGECY_SMS = 17;
    /**
     * @hide
     */
    public static final int OP_RECEIVE_MMS = 18;
    /**
     * @hide
     */
    public static final int OP_RECEIVE_WAP_PUSH = 19;
    /**
     * @hide
     */
    public static final int OP_SEND_SMS = 20;
    /**
     * @hide
     */
    public static final int OP_READ_ICC_SMS = 21;
    /**
     * @hide
     */
    public static final int OP_WRITE_ICC_SMS = 22;
    /**
     * @hide
     */
    public static final int OP_WRITE_SETTINGS = 23;
    /**
     * @hide
     */
    public static final int OP_SYSTEM_ALERT_WINDOW = 24;
    /**
     * @hide
     */
    public static final int OP_ACCESS_NOTIFICATIONS = 25;
    /**
     * @hide
     */
    public static final int OP_CAMERA = 26;
    /**
     * @hide
     */
    public static final int OP_RECORD_AUDIO = 27;
    /**
     * @hide
     */
    public static final int OP_PLAY_AUDIO = 28;
    /**
     * @hide
     */
    public static final int OP_READ_CLIPBOARD = 29;
    /**
     * @hide
     */
    public static final int OP_WRITE_CLIPBOARD = 30;
    /**
     * @hide
     */
    public static final int OP_TAKE_MEDIA_BUTTONS = 31;
    /**
     * @hide
     */
    public static final int OP_TAKE_AUDIO_FOCUS = 32;
    /**
     * @hide
     */
    public static final int OP_AUDIO_MASTER_VOLUME = 33;
    /**
     * @hide
     */
    public static final int OP_AUDIO_VOICE_VOLUME = 34;
    /**
     * @hide
     */
    public static final int OP_AUDIO_RING_VOLUME = 35;
    /**
     * @hide
     */
    public static final int OP_AUDIO_MEDIA_VOLUME = 36;
    /**
     * @hide
     */
    public static final int OP_AUDIO_ALARM_VOLUME = 37;
    /**
     * @hide
     */
    public static final int OP_AUDIO_NOTIFICATION_VOLUME = 38;
    /**
     * @hide
     */
    public static final int OP_AUDIO_BLUETOOTH_VOLUME = 39;
    /**
     * @hide
     */
    public static final int OP_WAKE_LOCK = 40;
    /**
     * @hide Continually monitoring location data.
     */
    public static final int OP_MONITOR_LOCATION = 41;
    /**
     * @hide Continually monitoring location data with a relatively high power request.
     */
    public static final int OP_MONITOR_HIGH_POWER_LOCATION = 42;
    /**
     * @hide Retrieve current usage stats via {@link UsageStatsManager}.
     */
    public static final int OP_GET_USAGE_STATS = 43;
    /**
     * @hide
     */
    public static final int OP_MUTE_MICROPHONE = 44;
    /**
     * @hide
     */
    public static final int OP_TOAST_WINDOW = 45;
    /**
     * @hide Capture the device's display contents and/or audio
     */
    public static final int OP_PROJECT_MEDIA = 46;
    /**
     * @hide Activate a VPN connection without user intervention.
     */
    public static final int OP_ACTIVATE_VPN = 47;
    /**
     * @hide Access the WallpaperManagerAPI to write wallpapers.
     */
    public static final int OP_WRITE_WALLPAPER = 48;
    /**
     * @hide Received the assist structure from an app.
     */
    public static final int OP_ASSIST_STRUCTURE = 49;
    /**
     * @hide Received a screenshot from assist.
     */
    public static final int OP_ASSIST_SCREENSHOT = 50;
    /**
     * @hide Read the phone state.
     */
    public static final int OP_READ_PHONE_STATE = 51;
    /**
     * @hide Add voicemail messages to the voicemail content provider.
     */
    public static final int OP_ADD_VOICEMAIL = 52;
    /**
     * @hide Access APIs for SIP calling over VOIP or WiFi.
     */
    public static final int OP_USE_SIP = 53;
    /**
     * @hide Intercept outgoing calls.
     */
    public static final int OP_PROCESS_OUTGOING_CALLS = 54;
    /**
     * @hide User the fingerprint API.
     */
    public static final int OP_USE_FINGERPRINT = 55;
    /**
     * @hide Access to body sensors such as heart rate, etc.
     */
    public static final int OP_BODY_SENSORS = 56;
    /**
     * @hide Read previously received cell broadcast messages.
     */
    public static final int OP_READ_CELL_BROADCASTS = 57;
    /**
     * @hide Inject mock location into the system.
     */
    public static final int OP_MOCK_LOCATION = 58;
    /**
     * @hide Read external storage.
     */
    public static final int OP_READ_EXTERNAL_STORAGE = 59;
    /**
     * @hide Write external storage.
     */
    public static final int OP_WRITE_EXTERNAL_STORAGE = 60;
    /**
     * @hide Turned on the screen.
     */
    public static final int OP_TURN_SCREEN_ON = 61;
    /**
     * @hide Get device accounts.
     */
    public static final int OP_GET_ACCOUNTS = 62;
    /**
     * @hide Control whether an application is allowed to run in the background.
     */
    public static final int OP_RUN_IN_BACKGROUND = 63;
    /**
     * @hide
     */
    public static final int OP_AUDIO_ACCESSIBILITY_VOLUME = 64;
    /**
     * @hide Read the phone number.
     */
    public static final int OP_READ_PHONE_NUMBERS = 65;
    /**
     * @hide Request package installs through package installer
     */
    public static final int OP_REQUEST_INSTALL_PACKAGES = 66;
    /**
     * @hide Enter picture-in-picture.
     */
    public static final int OP_PICTURE_IN_PICTURE = 67;
    /**
     * @hide Instant app start foreground service.
     */
    public static final int OP_INSTANT_APP_START_FOREGROUND = 68;
    /**
     * @hide Answer incoming phone calls
     */
    public static final int OP_ANSWER_PHONE_CALLS = 69;


    public static final int OP_READ_INSTALLED_APPS = 70;
    public static final int OP_GET_RUNNING_TASKS = 71;
    public static final int OP_GET_DEVICE_ID = 72;
    public static final int OP_GET_SIM_SERIAL_NUMBER = 73;
    public static final int OP_GET_LINE1_NUMBER = 74;
    public static final int OP_SET_ALARM = 75;
    public static final int OP_START_SERVICE = 76;
    public static final int OP_EXECUTE_SHELL_COMMAND = 77;
    public static final int OP_FC_DIALOG = 78;
    public static final int OP_CHANGE_BRIGHTNESS = 79;

    public static final int[] EXTRA_OPS = new int[]{
            OP_READ_INSTALLED_APPS,
            OP_GET_RUNNING_TASKS,
            OP_GET_DEVICE_ID,
            OP_GET_SIM_SERIAL_NUMBER,
            OP_GET_LINE1_NUMBER,
            OP_SET_ALARM,
            OP_START_SERVICE,
            OP_WAKE_LOCK,
            OP_EXECUTE_SHELL_COMMAND,
            OP_FC_DIALOG,
            OP_CHANGE_BRIGHTNESS,
    };

    public static final int CATEGORY_EXTRA = 0x1;
    public static final int CATEGORY_DEFAULT = 0x2;

    public static final int _NUM_OP = 80;
    public static final int _NUM_OP_DEF = 70;

    /**
     * Access to coarse location information.
     */
    public static final String OPSTR_COARSE_LOCATION = "android:coarse_location";
    /**
     * Access to fine location information.
     */
    public static final String OPSTR_FINE_LOCATION =
            "android:fine_location";
    /**
     * Continually monitoring location data.
     */
    public static final String OPSTR_MONITOR_LOCATION
            = "android:monitor_location";
    /**
     * Continually monitoring location data with a relatively high power request.
     */
    public static final String OPSTR_MONITOR_HIGH_POWER_LOCATION
            = "android:monitor_location_high_power";
    /**
     * Access to {@link UsageStatsManager}.
     */
    public static final String OPSTR_GET_USAGE_STATS
            = "android:get_usage_stats";
    /**
     * Activate a VPN connection without user intervention. @hideAndDetach
     */
    public static final String OPSTR_ACTIVATE_VPN
            = "android:activate_vpn";
    /**
     * Allows an application to read the user's contacts data.
     */
    public static final String OPSTR_READ_CONTACTS
            = "android:read_contacts";
    /**
     * Allows an application to write to the user's contacts data.
     */
    public static final String OPSTR_WRITE_CONTACTS
            = "android:write_contacts";
    /**
     * Allows an application to read the user's call log.
     */
    public static final String OPSTR_READ_CALL_LOG
            = "android:read_call_log";
    /**
     * Allows an application to write to the user's call log.
     */
    public static final String OPSTR_WRITE_CALL_LOG
            = "android:write_call_log";
    /**
     * Allows an application to read the user's calendar data.
     */
    public static final String OPSTR_READ_CALENDAR
            = "android:read_calendar";
    /**
     * Allows an application to write to the user's calendar data.
     */
    public static final String OPSTR_WRITE_CALENDAR
            = "android:write_calendar";
    /**
     * Allows an application to initiate a phone call.
     */
    public static final String OPSTR_CALL_PHONE
            = "android:call_phone";
    /**
     * Allows an application to read SMS messages.
     */
    public static final String OPSTR_READ_SMS
            = "android:read_sms";
    /**
     * Allows an application to receive SMS messages.
     */
    public static final String OPSTR_RECEIVE_SMS
            = "android:receive_sms";
    /**
     * Allows an application to receive MMS messages.
     */
    public static final String OPSTR_RECEIVE_MMS
            = "android:receive_mms";
    /**
     * Allows an application to receive WAP push messages.
     */
    public static final String OPSTR_RECEIVE_WAP_PUSH
            = "android:receive_wap_push";
    /**
     * Allows an application to send SMS messages.
     */
    public static final String OPSTR_SEND_SMS
            = "android:send_sms";
    /**
     * Required to be able to access the camera device.
     */
    public static final String OPSTR_CAMERA
            = "android:camera";
    /**
     * Required to be able to access the microphone device.
     */
    public static final String OPSTR_RECORD_AUDIO
            = "android:record_audio";
    /**
     * Required to access phone state related information.
     */
    public static final String OPSTR_READ_PHONE_STATE
            = "android:read_phone_state";
    /**
     * Required to access phone state related information.
     */
    public static final String OPSTR_ADD_VOICEMAIL
            = "android:add_voicemail";
    /**
     * Access APIs for SIP calling over VOIP or WiFi
     */
    public static final String OPSTR_USE_SIP
            = "android:use_sip";
    /**
     * Access APIs for diverting outgoing calls
     */
    public static final String OPSTR_PROCESS_OUTGOING_CALLS
            = "android:process_outgoing_calls";
    /**
     * Use the fingerprint API.
     */
    public static final String OPSTR_USE_FINGERPRINT
            = "android:use_fingerprint";
    /**
     * Access to body sensors such as heart rate, etc.
     */
    public static final String OPSTR_BODY_SENSORS
            = "android:body_sensors";
    /**
     * Read previously received cell broadcast messages.
     */
    public static final String OPSTR_READ_CELL_BROADCASTS
            = "android:read_cell_broadcasts";
    /**
     * Inject mock location into the system.
     */
    public static final String OPSTR_MOCK_LOCATION
            = "android:mock_location";
    /**
     * Read external storage.
     */
    public static final String OPSTR_READ_EXTERNAL_STORAGE
            = "android:read_external_storage";
    /**
     * Write external storage.
     */
    public static final String OPSTR_WRITE_EXTERNAL_STORAGE
            = "android:write_external_storage";
    /**
     * Required to draw on top of other apps.
     */
    public static final String OPSTR_SYSTEM_ALERT_WINDOW
            = "android:system_alert_window";
    /**
     * Required to write/modify/update system settingss.
     */
    public static final String OPSTR_WRITE_SETTINGS
            = "android:write_settings";
    /**
     * @hide Get device accounts.
     */
    public static final String OPSTR_GET_ACCOUNTS
            = "android:get_accounts";
    public static final String OPSTR_READ_PHONE_NUMBERS
            = "android:read_phone_numbers";
    /**
     * Access to picture-in-picture.
     */
    public static final String OPSTR_PICTURE_IN_PICTURE
            = "android:picture_in_picture";
    /**
     * @hide
     */
    public static final String OPSTR_INSTANT_APP_START_FOREGROUND
            = "android:instant_app_start_foreground";
    /**
     * Answer incoming phone calls
     */
    public static final String OPSTR_ANSWER_PHONE_CALLS
            = "android:answer_phone_calls";

    /**
     * This provides a simple name for each operation to be used
     * in debug output.
     */
    private static String[] sOpNames = new String[]{
            "COARSE_LOCATION",
            "FINE_LOCATION",
            "GPS",
            "VIBRATE",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_CALL_LOG",
            "WRITE_CALL_LOG",
            "READ_CALENDAR",
            "WRITE_CALENDAR",
            "WIFI_SCAN",
            "POST_NOTIFICATION",
            "NEIGHBORING_CELLS",
            "CALL_PHONE",
            "READ_SMS",
            "WRITE_SMS",
            "RECEIVE_SMS",
            "RECEIVE_EMERGECY_SMS",
            "RECEIVE_MMS",
            "RECEIVE_WAP_PUSH",
            "SEND_SMS",
            "READ_ICC_SMS",
            "WRITE_ICC_SMS",
            "WRITE_SETTINGS",
            "SYSTEM_ALERT_WINDOW",
            "ACCESS_NOTIFICATIONS",
            "CAMERA",
            "RECORD_AUDIO",
            "PLAY_AUDIO",
            "READ_CLIPBOARD",
            "WRITE_CLIPBOARD",
            "TAKE_MEDIA_BUTTONS",
            "TAKE_AUDIO_FOCUS",
            "AUDIO_MASTER_VOLUME",
            "AUDIO_VOICE_VOLUME",
            "AUDIO_RING_VOLUME",
            "AUDIO_MEDIA_VOLUME",
            "AUDIO_ALARM_VOLUME",
            "AUDIO_NOTIFICATION_VOLUME",
            "AUDIO_BLUETOOTH_VOLUME",
            "WAKE_LOCK",
            "MONITOR_LOCATION",
            "MONITOR_HIGH_POWER_LOCATION",
            "GET_USAGE_STATS",
            "MUTE_MICROPHONE",
            "TOAST_WINDOW",
            "PROJECT_MEDIA",
            "ACTIVATE_VPN",
            "WRITE_WALLPAPER",
            "ASSIST_STRUCTURE",
            "ASSIST_SCREENSHOT",
            "OP_READ_PHONE_STATE",
            "ADD_VOICEMAIL",
            "USE_SIP",
            "PROCESS_OUTGOING_CALLS",
            "USE_FINGERPRINT",
            "BODY_SENSORS",
            "READ_CELL_BROADCASTS",
            "MOCK_LOCATION",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "TURN_ON_SCREEN",
            "GET_ACCOUNTS",
            "RUN_IN_BACKGROUND",
            "AUDIO_ACCESSIBILITY_VOLUME",
            "READ_PHONE_NUMBERS",
            "REQUEST_INSTALL_PACKAGES",
            "PICTURE_IN_PICTURE",
            "INSTANT_APP_START_FOREGROUND",
            "ANSWER_PHONE_CALLS",

            // EXTRAS.
            "OP_READ_INSTALLED_APPS",
            "OP_GET_RUNNING_TASKS",
            "OP_GET_DEVICE_ID",
            "OP_GET_SIM_SERIAL_NUMBER",
            "OP_GET_LINE1_NUMBER",
            "OP_SET_ALARM",
            "OP_START_SERVICE",
            "OP_EXECUTE_SHELL_COMMAND",
            "OP_FC_DIALOG",
            "OP_CHANGE_BRIGHTNESS",
    };

    /**
     * This optionally maps a permission to an operation.  If there
     * is no permission associated with an operation, it is null.
     */
    private static String[] sOpPerms = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            null,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.ACCESS_WIFI_STATE,
            null, // no permission required for notifications
            null, // neighboring cells shares the coarse location perm
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,
            null, // no permission required for writing sms
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_EMERGENCY_BROADCAST,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            null, // no permission required for writing icc sms
//            android.Manifest.permission.WRITE_SETTINGS,
            null,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_NOTIFICATIONS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            null, // no permission for playing audio
            null, // no permission for reading clipboard
            null, // no permission for writing clipboard
            null, // no permission for taking media buttons
            null, // no permission for taking audio focus
            null, // no permission for changing master volume
            null, // no permission for changing voice volume
            null, // no permission for changing ring volume
            null, // no permission for changing media volume
            null, // no permission for changing alarm volume
            null, // no permission for changing notification volume
            null, // no permission for changing bluetooth volume
            Manifest.permission.WAKE_LOCK,
            null, // no permission for generic location monitoring
            null, // no permission for high power location monitoring
            Manifest.permission.PACKAGE_USAGE_STATS,
            null, // no permission for muting/unmuting microphone
            null, // no permission for displaying toasts
            null, // no permission for projecting media
            null, // no permission for activating vpn
            null, // no permission for supporting wallpaper
            null, // no permission for receiving assist structure
            null, // no permission for receiving assist screenshot
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.USE_FINGERPRINT,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.READ_CELL_BROADCASTS,
            null,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            null, // no permission for turning the screen on
            Manifest.permission.GET_ACCOUNTS,
            null, // no permission for running in background
            null, // no permission for changing accessibility volume
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            null, // no permission for entering picture-in-picture on hideAndDetach
            Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE,
            Manifest.permission.ANSWER_PHONE_CALLS,

            // EXTRAS.
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
    };

    /**
     * This specifies the default mode for each operation.
     */
    private static int[] sOpDefaultMode = new int[]{
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED, // OP_WRITE_SMS
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED, // OP_WRITE_SETTINGS
            AppOpsManager.MODE_ALLOWED, // OP_SYSTEM_ALERT_WINDOW
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED, // OP_GET_USAGE_STATS
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED, // OP_PROJECT_MEDIA
            AppOpsManager.MODE_ALLOWED, // OP_ACTIVATE_VPN
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,  // OP_MOCK_LOCATION
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,  // OP_TURN_ON_SCREEN
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,  // OP_RUN_IN_BACKGROUND
            AppOpsManager.MODE_ALLOWED,  // OP_AUDIO_ACCESSIBILITY_VOLUME
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,  // OP_REQUEST_INSTALL_PACKAGES
            AppOpsManager.MODE_ALLOWED,  // OP_PICTURE_IN_PICTURE
            AppOpsManager.MODE_ALLOWED,  // OP_INSTANT_APP_START_FOREGROUND
            AppOpsManager.MODE_ALLOWED, // ANSWER_PHONE_CALLS 69

            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
            AppOpsManager.MODE_ALLOWED,
    };

    private static final Set<Integer> sLoggableOpSet = Sets.newHashSet(
            // EXT
            OP_READ_INSTALLED_APPS,
            OP_GET_RUNNING_TASKS,
            OP_GET_DEVICE_ID,
            OP_GET_SIM_SERIAL_NUMBER,
            OP_GET_LINE1_NUMBER,
            OP_SET_ALARM,
            OP_START_SERVICE,
            OP_WAKE_LOCK,
            OP_FC_DIALOG,
            OP_CHANGE_BRIGHTNESS
    );

    /**
     * Mapping from a permission to the corresponding app op.
     */
    private static HashMap<String, Integer> sPermToOp = new HashMap<>();

    static {
        if (sOpNames.length != _NUM_OP) {
            throw new IllegalStateException("sOpNames length " + sOpNames.length
                    + " should be " + _NUM_OP);
        }
        if (sOpPerms.length != _NUM_OP) {
            throw new IllegalStateException("sOpPerms length " + sOpPerms.length
                    + " should be " + _NUM_OP);
        }
        if (sOpDefaultMode.length != _NUM_OP) {
            throw new IllegalStateException("sOpDefaultMode length " + sOpDefaultMode.length
                    + " should be " + _NUM_OP);
        }
        for (int i = 0; i < _NUM_OP; i++) {
            if (sOpPerms[i] != null) {
                sPermToOp.put(sOpPerms[i], i);
            }
        }
    }

    public static int[] getDefaultModes() {
        return sOpDefaultMode;
    }

    /**
     * Retrieve the app op code for a permission, or null if there is not one.
     * This API is intended to be used for mapping runtime or appop permissions
     * to the corresponding app op.
     */
    public static int permissionToOpCode(String permission) {
        Integer boxedOpCode = sPermToOp.get(permission);
        return boxedOpCode != null ? boxedOpCode : OP_NONE;
    }

    /**
     * Retrieve the permission associated with an operation, or null if there is not one.
     */
    public static String opToPermission(int op) {
        if (op >= sOpPerms.length) return "UNKNOWN";
        return sOpPerms[op];
    }

    private XAppOpsManager(Context context) {
    }

    private static XAppOpsManager sMe = null;

    @Synchronized
    public static XAppOpsManager from(Context context) {
        if (sMe == null) sMe = new XAppOpsManager(context);
        return sMe;
    }

    public static boolean isLoggableOp(int op) {
        return true;
    }
}
