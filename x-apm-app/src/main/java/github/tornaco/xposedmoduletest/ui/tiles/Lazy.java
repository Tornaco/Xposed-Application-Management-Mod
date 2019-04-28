package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.RecentTile;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.activity.lazy.LazyAppNavActivity;
import github.tornaco.xposedmoduletest.ui.activity.lazy.LazyRuleNavActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class Lazy extends QuickTile {

    public Lazy(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_lazy;
        if (XAPMManager.get().isServiceAvailable()) {
            this.summaryRes = XAPMManager.get().isLazyModeEnabled() ?
                    R.string.summary_func_enabled : 0;
        }
        this.iconRes = R.drawable.ic_user_smile_fill;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_orange;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, LazyAppNavActivity.class));
                // Save to recent.
                AppSettings.addRecentTile(context, RecentTile.from(TileManager.getTileKey(Lazy.class)));
            }

            @Override
            public boolean onLongClick(View v) {
                showPopMenu(v);
                return true;
            }
        };


    }

    private void showPopMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(getPopupMenuRes());
        popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener(anchor.getContext()));
        popupMenu.show();
    }

    private PopupMenu.OnMenuItemClickListener
    onCreateOnMenuItemClickListener(final Context context) {
        return item -> {
            if (item.getItemId() == R.id.action_rules) {
                LazyRuleNavActivity.start(context);
                return true;
            }
            return false;
        };
    }

    private int getPopupMenuRes() {
        return R.menu.tile_lazy;
    }
}
