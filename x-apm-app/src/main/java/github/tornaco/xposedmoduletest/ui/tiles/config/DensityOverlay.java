package github.tornaco.xposedmoduletest.ui.tiles.config;

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

public class DensityOverlay extends QuickTile {

    public DensityOverlay(final Context context, final String pkg) {
        super(context);
        this.titleRes = R.string.title_density_overlay;
        this.iconRes = R.drawable.ic_fiber_smart_record_black_24dp;
        if (XAPMManager.get().isServiceAvailable()) {
            int density = XAPMManager.get().getAppConfigOverlayIntSetting(pkg, "densityDpi");
            if (density != XAPMManager.ConfigOverlays.NONE) {
                this.summary = String.valueOf(density);
            } else {
                this.summary = "DEFAULT";
            }
        }
        this.tileView = new EditTextTileView(context) {

            @Override
            protected int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected CharSequence getHint() {
                return "输入0重置为默认数值";
            }

            @Override
            protected CharSequence getDialogTitle() {
                return context.getResources().getString(R.string.summary_density_overlay);
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
                    int density = Integer.parseInt(text);
                    if (density < 0) {
                        Toast.makeText(context, R.string.summary_density_overlay_should_be_positive, Toast.LENGTH_SHORT).show();
                    } else {
                        XAPMManager.get().setAppConfigOverlayIntSetting(pkg, "densityDpi",
                                density == 0 ? XAPMManager.ConfigOverlays.NONE : density);

                        // Update summary.
                        final String delayMillsStr =
                                density == 0 ? "DEFAULT" : String.valueOf(density);
                        getSummaryTextView().setText(delayMillsStr);
                    }
                } catch (Throwable e) {
                    Toast.makeText(context, R.string.summary_density_overlay_fail, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
