package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.perm.Apps2OpListActivity;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class PermControlTemplate extends QuickTile {
    @Getter
    private String pkgName;

    public PermControlTemplate(final Context context, final String pkgName) {
        super(context);
        this.pkgName = pkgName;
        this.titleRes = R.string.title_perm_control_template;
        this.iconRes = R.drawable.ic_beenhere_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                Apps2OpListActivity.start(context, pkgName);
            }
        };
    }
}
