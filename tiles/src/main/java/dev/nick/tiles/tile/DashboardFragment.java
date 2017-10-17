package dev.nick.tiles.tile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.R;

public class DashboardFragment extends Fragment {

    private LayoutInflater mLayoutInflater;
    private ViewGroup mDashboard;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buildUI(getActivity());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLayoutInflater = inflater;

        final View rootView = inflater.inflate(getLayoutId(), container, false);
        mDashboard = (ViewGroup) rootView.findViewById(R.id.dashboard_container);

        return rootView;
    }


    protected int getLayoutId() {
        return R.layout.dashboard;
    }

    private List<Category> getDashboardCategories() {
        List<Category> categories = new ArrayList<Category>();
        onCreateDashCategories(categories);
        return categories;
    }

    protected void onCreateDashCategories(List<Category> categories) {
        // Need an impl.
    }

    protected void buildUI(Context context) {
        if (!isAdded()) {
            throw new IllegalStateException("Fragment not added yet.");
        }

        long start = System.currentTimeMillis();
        final Resources res = getResources();

        mDashboard.removeAllViews();

        final List<Category> categories = getDashboardCategories();

        final int count = categories.size();

        for (int n = 0; n < count; n++) {
            final Category category = categories.get(n);

            View categoryView = mLayoutInflater.inflate(R.layout.dashboard_category, mDashboard,
                    false);

            TextView categoryLabel = (TextView) categoryView.findViewById(R.id.category_title);
            if (category.getTitle(res) != null)
                categoryLabel.setText(category.getTitle(res));
            else {
                categoryLabel.setVisibility(View.GONE);
            }

            final TextView categorySummary = (TextView) categoryView.findViewById(R.id.category_summary);

            if (category.getSummary(res) != null) {
                categorySummary.setText(category.getSummary(res));
                category.onSummarySet(categorySummary);

            } else {
                categorySummary.setVisibility(View.GONE);
            }

            ViewGroup categoryContent =
                    (ViewGroup) categoryView.findViewById(R.id.category_content);

            final int tilesCount = category.getTilesCount();
            for (int i = 0; i < tilesCount; i++) {
                Tile tile = category.getTile(i);
                TileView tileView = tile.tileView;
                updateTileView(context, res, tile, tileView.getImageView(),
                        tileView.getTitleTextView(), tileView.getSummaryTextView());

                categoryContent.addView(tileView);
            }

            // Add the category
            mDashboard.addView(categoryView);
        }
        long delta = System.currentTimeMillis() - start;
        onUIBuilt();
    }

    protected void onUIBuilt() {
        // None
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateTileView(Context context,
                                Resources res, final Tile tile,
                                ImageView tileIcon, TextView tileTextView, TextView statusTextView) {

        if (tileIcon != null) {
            if (tile.iconRes > 0) {
                tileIcon.setImageResource(tile.iconRes);
            } else if (tile.iconDrawable != null) {
                tileIcon.setImageDrawable(tile.iconDrawable);
            } else {
                tileIcon.setImageDrawable(null);
                tileIcon.setBackground(null);
            }
        }

        if (tileTextView != null) {
            tileTextView.setText(tile.getTitle(res));
        }

        if (statusTextView != null) {
            CharSequence summary = tile.getSummary(res);
            if (!TextUtils.isEmpty(summary)) {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(summary);
            } else {
                statusTextView.setVisibility(View.GONE);
            }
        }
    }

}
