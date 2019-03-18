package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.XAppGithubCommitSha;

/**
 * Created by guohao4 on 2017/8/2.
 * Email: Tornaco@163.com
 */

public class GitContributors extends QuickTile {

    public GitContributors(final Context context) {
        super(context);
        this.titleRes = R.string.title_git_contributors;
        this.summary = XAppGithubCommitSha.CONTRIBUTORS;
        this.iconRes = R.drawable.ic_github_fill;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_amber;
            }
        };
    }
}
