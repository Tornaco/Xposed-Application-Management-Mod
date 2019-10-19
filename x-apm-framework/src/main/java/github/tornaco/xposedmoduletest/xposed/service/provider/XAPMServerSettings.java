package github.tornaco.xposedmoduletest.xposed.service.provider;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/5/15 13:35.
 * God bless no bug!
 */
public class XAPMServerSettings {

    // Boolean fields.

    // 省电模式
    public static final BaseSetting<Boolean> APM_POWER_SAVE_B = new BooleanSettingImpl("APM_POWER_SAVE_B", true);
    // 关联启动规则
    public static final BaseSetting<Boolean> APM_START_RULE_B = new BooleanSettingImpl("APM_START_RULE_B", false);
    // 乖巧规则
    public static final BaseSetting<Boolean> APM_LAZY_RULE_B = new BooleanSettingImpl("APM_LAZY_RULE_B", false);
    // 应用锁
    public static final BaseSetting<Boolean> APP_GUARD_ENABLED_NEW_B = new BooleanSettingImpl("APP_GUARD_ENABLED_NEW_B", false);
    // 调试模式
    public static final BaseSetting<Boolean> APP_GUARD_DEBUG_MODE_B_S = new BooleanSettingImpl("APP_GUARD_DEBUG_MODE_B_S", false);
    // 卸载阻止
    public static final BaseSetting<Boolean> UNINSTALL_GUARD_ENABLED_B = new BooleanSettingImpl("UNINSTALL_GUARD_ENABLED_B", false);
    // 模糊
    public static final BaseSetting<Boolean> BLUR_ENABLED_B = new BooleanSettingImpl("BLUR_ENABLED_B", false);
    // 乖巧方案A
    public static final BaseSetting<Boolean> APM_LAZY_SOLUTION_APP_B = new BooleanSettingImpl("APM_LAZY_SOLUTION_APP_B", false);
    // 乖巧方案B
    public static final BaseSetting<Boolean> APM_LAZY_SOLUTION_FW_B = new BooleanSettingImpl("APM_LAZY_SOLUTION_FW_B", true);
    // 锁屏清理
    public static final BaseSetting<Boolean> LOCK_KILL_ENABLED_B = new BooleanSettingImpl("LOCK_KILL_ENABLED_B", false);
    // 绿化（已经废弃）
    @Deprecated
    public static final BaseSetting<Boolean> GREENING_ENABLED_B = new BooleanSettingImpl("GREENING_ENABLED_B", false);
    // 隐匿
    public static final BaseSetting<Boolean> PRIVACY_ENABLED_B = new BooleanSettingImpl("PRIVACY_ENABLED_B", false);
    // 乖巧模式
    public static final BaseSetting<Boolean> LAZY_ENABLED_B = new BooleanSettingImpl("LAZY_ENABLED_B", false);
    // 锁屏清理，保护音频
    public static final BaseSetting<Boolean> LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B = new BooleanSettingImpl("LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B", false);
    // 保护系统应用
    public static final BaseSetting<Boolean> ASH_WHITE_SYS_APP_ENABLED_B = new BooleanSettingImpl("ASH_WHITE_SYS_APP_ENABLED_B", true);
    // 保护有通知的应用
    public static final BaseSetting<Boolean> ASH_WONT_KILL_SBN_APP_B = new BooleanSettingImpl("ASH_WONT_KILL_SBN_APP_B", true);
    public static final BaseSetting<Boolean> ASH_WONT_KILL_SBN_APP_GREEN_B = new BooleanSettingImpl("ASH_WONT_KILL_SBN_APP_GREEN_B", false);
    // 返回强退
    public static final BaseSetting<Boolean> ROOT_ACTIVITY_KILL_ENABLED_B = new BooleanSettingImpl("ROOT_ACTIVITY_KILL_ENABLED_B", false);
    // 划卡
    public static final BaseSetting<Boolean> REMOVE_TASK_KILL_ENABLED_B = new BooleanSettingImpl("REMOVE_TASK_KILL_ENABLED_B", false);
    // 长按返回退出
    public static final BaseSetting<Boolean> LONG_PRESS_BACK_KILL_ENABLED_B = new BooleanSettingImpl("LONG_PRESS_BACK_KILL_ENABLED_B", false);
    // 阻止组件设置
    public static final BaseSetting<Boolean> COMP_SETTING_BLOCK_ENABLED_B = new BooleanSettingImpl("COMP_SETTING_BLOCK_ENABLED_B", false);
    // 通知亮屏
    public static final BaseSetting<Boolean> WAKE_UP_ON_NOTIFICATION_POSTED_ENABLED_B = new BooleanSettingImpl("WAKE_UP_ON_NOTIFICATION_POSTED_ENABLED_B", false);
    // 自启动
    public static final BaseSetting<Boolean> BOOT_BLOCK_ENABLED_B = new BooleanSettingImpl("BOOT_BLOCK_ENABLED_B", false);
    // 关联启动
    public static final BaseSetting<Boolean> START_BLOCK_ENABLED_B = new BooleanSettingImpl("START_BLOCK_ENABLED_B", false);

    public static final BaseSetting<Boolean> INTERRUPT_FP_SUCCESS_VB_ENABLED_B = new BooleanSettingImpl("INTERRUPT_FP_SUCCESS_VB_ENABLED_B", false);
    public static final BaseSetting<Boolean> INTERRUPT_FP_ERROR_VB_ENABLED_B = new BooleanSettingImpl("INTERRUPT_FP_ERROR_VB_ENABLED_B", false);

    public static final BaseSetting<Boolean> AUTO_BLACK_FOR_NEW_INSTALLED_APP_B = new BooleanSettingImpl("AUTO_BLACK_FOR_NEW_INSTALLED_APP_B", false);
    public static final BaseSetting<Boolean> AUTO_BLACK_NOTIFICATION_FOR_NEW_INSTALLED_APP_B = new BooleanSettingImpl("AUTO_BLACK_NOTIFICATION_FOR_NEW_INSTALLED_APP_B", true);

    public static final BaseSetting<Boolean> DATA_MIGRATE_B = new BooleanSettingImpl("DATA_MIGRATE_B", false);

    public static final BaseSetting<Boolean> PERMISSION_CONTROL_B = new BooleanSettingImpl("PERMISSION_CONTROL_B", false);

    public static final BaseSetting<Boolean> SHOW_FOCUSED_ACTIVITY_INFO_B = new BooleanSettingImpl("SHOW_FOCUSED_ACTIVITY_INFO_B", false);
    public static final BaseSetting<Boolean> SHOW_CRASH_DUMP_B = new BooleanSettingImpl("SHOW_CRASH_DUMP_B", false);

    public static final BaseSetting<Boolean> NETWORK_RESTRICT_B = new BooleanSettingImpl("NETWORK_RESTRICT_B", false);

    // DOZE
    public static final BaseSetting<Boolean> APM_DOZE_ENABLE_B = new BooleanSettingImpl("APM_DOZE_ENABLE_B", false);
    public static final BaseSetting<Boolean> APM_FORCE_DOZE_ENABLE_B = new BooleanSettingImpl("APM_FORCE_DOZE_ENABLE_B", false);
    public static final BaseSetting<Boolean> APM_DISABLE_MOTION_ENABLE_B = new BooleanSettingImpl("APM_DISABLE_MOTION_ENABLE_B", false);

    // 常驻内存
    public static final BaseSetting<Boolean> APM_RESIDENT_B = new BooleanSettingImpl("APM_RESIDENT_B", false);

    public static final BaseSetting<Boolean> APM_PANIC_LOCK_B = new BooleanSettingImpl("APM_PANIC_LOCK_B", false);
    public static final BaseSetting<Boolean> APM_PANIC_HOME_B = new BooleanSettingImpl("APM_PANIC_HOME_B", false);

    // 清理通知
    public static final BaseSetting<Boolean> APM_SHOW_APP_PROCESS_UPDATE_B = new BooleanSettingImpl("APM_SHOW_APP_PROCESS_UPDATE_B", false);

    // 自救
    public static final BaseSetting<Boolean> REDEMPTION_ENABLED = new BooleanSettingImpl("REDEMPTION_ENABLED", true);

    /**
     * 眠代替KILL
     *
     * @deprecated Use {@link XAPMManager.OPT#APP_INACTIVE_POLICY_} instead.
     */
    @Deprecated
    public static final BaseSetting<Boolean> INACTIVE_INSTEAD_OF_FORCE_STOP = new BooleanSettingImpl("INACTIVE_INSTEAD_OF_FORCE_STOP", false);

    // Int fields.
    public static final BaseSetting<Integer> BLUR_RADIUS_I = new IntSettingImpl("BLUR_RADIUS_I_RS", BlurSettings.BLUR_RADIUS);
    public static final BaseSetting<Integer> ASH_CONTROL_MODE_I = new IntSettingImpl("ASH_CONTROL_MODE_I", XAPMManager.ControlMode.BLACK_LIST);

    // String fields.
    public static final BaseSetting<String> USER_DEFINED_LINE1_NUM_T_S = new StringSettingImpl("USER_DEFINED_LINE1_NUM_T_S", "18888888888");
    public static final BaseSetting<String> USER_DEFINED_DEVICE_ID_T_S = new StringSettingImpl("USER_DEFINED_DEVICE_ID_T_S", "fuckit");
    public static final BaseSetting<String> USER_DEFINED_ANDROID_ID_T_S = new StringSettingImpl("USER_DEFINED_ANDROID_ID_T_S", "xxxxxxxxxx");


    // Long fields.
    public static final BaseSetting<Long> DOZE_DELAY_L = new LongSettingImpl("DOZE_DELAY_L", 5 * 60 * 1000L);
    public static final BaseSetting<Long> LOCK_KILL_DELAY_L = new LongSettingImpl("LOCK_KILL_DELAY_L", 0L);

    interface Setting<T> {
        T read(String key, T defValue);

        void write(String key, T value);
    }

    @AllArgsConstructor
    @Getter
    public static abstract class BaseSetting<T> implements Setting<T> {
        private String key;
        private T defaultValue;

        public T read() {
            return read(key, defaultValue);
        }

        public void write(T value) {
            write(key, value);
        }
    }

    static class BooleanSettingImpl extends BaseSetting<Boolean> {

        BooleanSettingImpl(String key, Boolean defaultValue) {
            super(key, defaultValue);
        }

        @Override
        public Boolean read(String key, Boolean defValue) {
            return SettingsProvider.get().getBoolean(key, defValue);
        }

        @Override
        public void write(String key, Boolean value) {
            SettingsProvider.get().putBoolean(key, value);
        }
    }

    static class IntSettingImpl extends BaseSetting<Integer> {

        IntSettingImpl(String key, Integer defaultValue) {
            super(key, defaultValue);
        }

        @Override
        public Integer read(String key, Integer defValue) {
            return SettingsProvider.get().getInt(key, defValue);
        }

        @Override
        public void write(String key, Integer value) {
            SettingsProvider.get().putInt(key, value);
        }
    }

    static class StringSettingImpl extends BaseSetting<String> {

        StringSettingImpl(String key, String defaultValue) {
            super(key, defaultValue);
        }

        @Override
        public String read(String key, String defValue) {
            return SettingsProvider.get().getString(key, defValue);
        }

        @Override
        public void write(String key, String value) {
            SettingsProvider.get().putString(key, value);
        }
    }

    static class LongSettingImpl extends BaseSetting<Long> {

        LongSettingImpl(String key, Long defaultValue) {
            super(key, defaultValue);
        }

        @Override
        public Long read(String key, Long defValue) {
            return SettingsProvider.get().getLong(key, defValue);
        }

        @Override
        public void write(String key, Long value) {
            SettingsProvider.get().putLong(key, value);
        }
    }
}
