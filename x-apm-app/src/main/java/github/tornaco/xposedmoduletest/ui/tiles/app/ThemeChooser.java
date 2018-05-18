package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.Themes;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class ThemeChooser extends QuickTile {

    private List<String> descList;

    private boolean firstHook = true;

    public ThemeChooser(final Context context) {
        super(context);

        descList = Themes.getThemeNames(context);

        this.titleRes = R.string.title_theme;
        this.iconRes = R.drawable.ic_palette_green_24dp;

        final Themes currentTheme = XSettings.getThemes(context);
        this.summaryRes = currentTheme.getThemeName();

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(Themes.indexOf(currentTheme), true);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return descList;
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);

                if (firstHook) {
                    firstHook = false;
                    return;
                }

                Themes newTheme = Themes.ofIndex(position);

                if (newTheme != currentTheme) {
                    XSettings.setThemes(context, newTheme);

                    getTileView().getSummaryTextView().setText(descList.get(position));


                    try {
                        Activity activity = (Activity) context;
                        activity.recreate();
                    } catch (Throwable ignored) {

                    } finally {
                        Toast.makeText(context, R.string.title_theme_need_restart_app, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }
}
