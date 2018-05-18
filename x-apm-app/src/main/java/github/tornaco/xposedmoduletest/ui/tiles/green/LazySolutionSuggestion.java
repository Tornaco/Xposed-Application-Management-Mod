package github.tornaco.xposedmoduletest.ui.tiles.green;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by Tornaco on 2018/4/28 13:36.
 * God bless no bug!
 */
public class LazySolutionSuggestion extends QuickTile {

    public LazySolutionSuggestion(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.title_lazy_solution_suggestion;
        this.summaryRes = R.string.summary_lazy_solution_suggestion;
        this.iconRes = R.drawable.ic_help_black_24dp;

        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_amber;
            }
        };
        this.tileView.setEnabled(false);
        this.tileView.getTitleTextView().setEnabled(false);
        this.tileView.getSummaryTextView().setEnabled(false);
        this.tileView.getImageView().setEnabled(false);

    }
}
