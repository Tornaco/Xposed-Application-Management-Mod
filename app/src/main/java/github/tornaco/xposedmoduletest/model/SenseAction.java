package github.tornaco.xposedmoduletest.model;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/12/28.
 * Email: Tornaco@163.com
 */

public enum SenseAction implements StringResProvider {

    GPS_ON {
        @Override
        public int getStringRes() {
            return R.string.action_gps_on;
        }
    },
    GPS_OFF {
        @Override
        public int getStringRes() {
            return R.string.action_gps_off;
        }
    },

    BT_ON {
        @Override
        public int getStringRes() {
            return R.string.action_bt_on;
        }
    },
    BT_OFF {
        @Override
        public int getStringRes() {
            return R.string.action_bt_off;
        }
    },

    WIFI_ON {
        @Override
        public int getStringRes() {
            return R.string.action_wifi_on;
        }
    },
    WIFI_OFF {
        @Override
        public int getStringRes() {
            return R.string.action_wifi_off;
        }
    },

    DATA_ON {
        @Override
        public int getStringRes() {
            return R.string.action_data_on;
        }
    },
    DATA_OFF {
        @Override
        public int getStringRes() {
            return R.string.action_data_off;
        }
    },

    CRASH_THIS_APP {
        @Override
        public int getStringRes() {
            return R.string.action_crash;
        }
    },

//    SHELL {
//        @Override
//        public int getStringRes() {
//            return 0;
//        }
//    },
}
