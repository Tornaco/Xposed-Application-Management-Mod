package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.newstand.logger.Logger;

import java.util.List;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.ui.iconpack.IconPackManager;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class IconPack extends QuickTile {

    public IconPack(final Context context) {
        super(context);
        this.titleRes = R.string.title_app_icon_pack;
        this.iconRes = R.drawable.ic_launch_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                IconPackManager iconPackManager = IconPackManager.getInstance();
                final List<github.tornaco.xposedmoduletest.ui.iconpack.IconPack> packs =
                        iconPackManager.getAvailableIconPacks(context);
                final String[] targetIconPackPackage = {null};
                if (packs.size() == 0) {
                    Toast.makeText(context, R.string.title_app_icon_pack_noop, Toast.LENGTH_SHORT).show();
                } else {
                    int defIndex = packs.size();
                    String currentIconPack = AppSettings.getAppIconPack(context);
                    final String[] source = new String[packs.size() + 1];
                    for (int i = 0; i < source.length - 1; i++) {
                        source[i] = String.valueOf(packs.get(i).label);
                        if (currentIconPack != null) {
                            if (packs.get(i).packageName.equals(currentIconPack)) {
                                defIndex = i;
                            }
                        }
                    }
                    source[source.length - 1] = context.getString(R.string.title_app_icon_pack_disable);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.title_app_icon_pack)
                            .setSingleChoiceItems(source, defIndex,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == source.length - 1) {
                                                targetIconPackPackage[0] = null;
                                            } else {
                                                targetIconPackPackage[0] = packs.get(which).packageName;
                                            }
                                        }
                                    })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Logger.d("Set icon pack: " + targetIconPackPackage[0]);
                                    AppSettings.setAppIconPack(context, targetIconPackPackage[0]);

                                    // Clear glide cache.
                                    Glide.get(context).clearMemory();
                                    Toast.makeText(context, R.string.title_app_icon_pack_workaround, Toast.LENGTH_SHORT).show();
                                    XExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Glide.get(context).clearDiskCache();
                                        }
                                    });
                                }
                            })
                            .show();
                }
            }
        };
    }
}
