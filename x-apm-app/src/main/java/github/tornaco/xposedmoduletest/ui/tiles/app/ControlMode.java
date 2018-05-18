package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.widget.RelativeLayout;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class ControlMode extends QuickTile {

    public ControlMode(final Context context) {
        super(context);

        this.titleRes = R.string.title_control_mode;
        this.iconRes = R.drawable.ic_format_list_bulleted_black_24dp;

        final int currentMode =
                XAPMManager.get().isServiceAvailable() ?
                        XAPMManager.get().getControlMode()
                        : XAPMManager.ControlMode.UNKNOWN;

        String modeStr = currentMode == XAPMManager.ControlMode.WHITE_LIST ?
                context.getString(R.string.summary_control_mode_w) :
                context.getString(R.string.summary_control_mode_b);
        this.summary = context.getString(R.string.summary_control_mode, modeStr);

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(currentMode == XAPMManager.ControlMode.WHITE_LIST);
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                int newMode = checked ? XAPMManager.ControlMode.WHITE_LIST : XAPMManager.ControlMode.BLACK_LIST;
                if (XAPMManager.get().isServiceAvailable()) {
                    XAPMManager.get().setControlMode(newMode);
                }

                // Update summary.
                String modeStr = checked ?
                        context.getString(R.string.summary_control_mode_w) :
                        context.getString(R.string.summary_control_mode_b);
                getSummaryTextView().setText(context.getString(R.string.summary_control_mode, modeStr));
            }
        };
    }
}
