package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.Activity;
import android.view.View;

import org.newstand.logger.Logger;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.perm.AppOpsTemplatePicker;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class AppOpsTemplateSetting extends QuickTile {

    private AppOpsTemplate template = null;

    public AppOpsTemplateSetting(final Activity context, AppSettings settings) {
        super(context);
        this.titleRes = R.string.title_perm_control_template;

        final boolean isDonateOrPlay = github.tornaco.xposedmoduletest.provider.AppSettings.isDonated(getContext())
                || XAPMApplication.isPlayVersion();
        if (!isDonateOrPlay) {
            this.summaryRes = R.string.donated_available;
        } else {
            if (settings.getAppOpsTemplateId() != null) {
                template = XAPMManager.get().getAppOpsTemplateById(settings.getAppOpsTemplateId());
            }
            if (template == null) {
                this.summaryRes = R.string.summary_ops_template_not_set;
            } else {
                this.summary = context.getString(R.string.summary_ops_template_set, template.getAlias());
            }
        }

        this.iconRes = R.drawable.ic_beenhere_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (isDonateOrPlay) {
                    AppOpsTemplatePicker.chooseOne(context, template, template -> {
                        if (template != null) {
                            settings.setAppOpsTemplateId(template.getId());
                            getSummaryTextView().setText(context.getString(R.string.summary_ops_template_set, template.getAlias()));
                        } else {
                            getSummaryTextView().setText(R.string.summary_ops_template_not_set);
                            settings.setAppOpsTemplateId(null);
                        }
                        Logger.d("Selected template: " + settings);
                        XAPMManager.get().setAppInstalledAutoApplyTemplate(settings);
                    });
                }
            }
        };
    }
}
