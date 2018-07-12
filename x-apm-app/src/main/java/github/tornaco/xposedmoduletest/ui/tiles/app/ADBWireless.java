package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by Baozisoftware on 2018/05/25.
 */


public class ADBWireless extends QuickTile {
    public ADBWireless(final Context context) {
        super(context);
        this.titleRes = R.string.title_adb_wireless;
        this.iconRes = R.drawable.ic_adb_wireless_black_24dp;
        this.summary = context.getString(R.string.summary_adb_wireless) + getIP();
        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                boolean ok = getprop("service.adb.tcp.port", "-1").equals("5555");
                setChecked(ok);
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                String cmd = checked ? "setprop service.adb.tcp.port 5555 && stop adbd && start adbd" : "setprop service.adb.tcp.port -1 && stop adbd && start adbd";
                boolean ok = runRootCommand(cmd);
                Toast.makeText(context, ok ? R.string.adb_wireless_shell_success : R.string.adb_wireless_shell_fail, Toast.LENGTH_SHORT).show();
                if (ok) {
                    super.onCheckChanged(checked);
                }
            }
        };
    }

    private boolean runRootCommand(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream stream = new DataOutputStream(process.getOutputStream());
            stream.writeChars(cmd + "\n");
            stream.writeChars("exit\n");
            stream.flush();
            return true;
        } catch (Exception ex) {
        }
        return true;
    }

    private String getprop(String name, String defaultValue) {
        ArrayList processList = new ArrayList();
        String line;
        Pattern pattern = Pattern.compile("\\[(.+)\\]: \\[(.+)\\]");
        Matcher m;

        try {
            Process p = Runtime.getRuntime().exec("getprop");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                processList.add(line);
                m = pattern.matcher(line);
                if (m.find()) {
                    MatchResult result = m.toMatchResult();
                    if (result.group(1).equals(name))
                        return result.group(2);
                }
            }
            input.close();
        } catch (Exception ex) {
        }

        return defaultValue;
    }

    private String getIP() {
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            return ip;
        }
        return "-.-.-.-";
    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
