package github.tornaco.xposedmoduletest.ui.tiles.doze;

import android.content.Context;
import android.text.InputType;
import android.widget.Toast;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.QuickTile;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class DozeDelayTile extends QuickTile {

    public DozeDelayTile(final Context context) {
        super(context);
        this.titleRes = R.string.title_delay;
        final long delayMills = XAPMManager.get()
                .isServiceAvailable() ?
                XAPMManager.get().getDozeDelayMills()
                : -1;
        final String delayMillsStr = delayMills >= 0
                ? context.getResources().getString(R.string.summary_delay_mills, String.valueOf(delayMills))
                : context.getResources().getString(R.string.summary_delay_unknown);
        this.summary = context.getResources().getString(R.string.summary_delay, delayMillsStr);
        this.iconRes = R.drawable.ic_timer_black_24dp;

        this.tileView = new EditTextTileView(context) {

            @Override
            protected int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected CharSequence getHint() {
                return delayMillsStr;
            }

            @Override
            protected CharSequence getDialogTitle() {
                return context.getResources().getString(R.string.summary_delay_config_title);
            }

            @Override
            protected CharSequence getPositiveButton() {
                return context.getResources().getString(android.R.string.ok);
            }

            @Override
            protected CharSequence getNegativeButton() {
                return context.getResources().getString(android.R.string.cancel);
            }

            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                String text = getEditText().getText().toString();
                Logger.d("onPositiveButtonClick: " + text);
                try {
                    long delay = Long.parseLong(text);
                    if (delay < 0) {
                        Toast.makeText(context, R.string.summary_delay_should_be_positive, Toast.LENGTH_SHORT).show();
                    } else {
                        XAPMManager.get().setDozeDelayMills(delay);

                        // Update summary.
                        final String delayMillsStr = delay > 0
                                ? context.getResources().getString(R.string.summary_delay_mills, String.valueOf(delay))
                                : context.getResources().getString(R.string.summary_delay_unknown);
                        getSummaryTextView().setText(context.getResources().getString(R.string.summary_delay, delayMillsStr));
                    }
                } catch (Throwable e) {
                    Toast.makeText(context, R.string.summary_delay_invalid, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
