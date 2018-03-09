/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.tiles.tile;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Category {

    public static final long CAT_ID_UNDEFINED = -1;
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    public long id = CAT_ID_UNDEFINED;
    /**
     * Resource ID of title of the category that is shown to the user.
     */
    public int titleRes;
    /**
     * Title of the category that is shown to the user.
     */
    public CharSequence title;
    public CharSequence summary;
    public int summaryRes;
    public int moreDrawableRes;
    /**
     * Number of column for this category, force use instead of num column of dashboard.
     */
    public int numColumns;
    public View.OnClickListener onMoreButtonClickListener;
    /**
     * List of the category's children
     */
    public List<Tile> tiles = new ArrayList<Tile>();

    public Category() {
        // Empty
    }

    Category(Parcel in) {
        readFromParcel(in);
    }

    int tileCount() {
        return tiles == null ? 0 : tiles.size();
    }

    public void addTile(Tile tile) {
        if (tile.isEnabled()) {
            tiles.add(tile);
        }
    }

    public void addTile(int n, Tile tile) {
        tiles.add(n, tile);
    }

    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    public void removeTile(int n) {
        tiles.remove(n);
    }

    public int getTilesCount() {
        return tiles.size();
    }

    public Tile getTile(int n) {
        return tiles.get(n);
    }

    /**
     * Return the currently set title.  If {@link #titleRes} is set,
     * this resource is loaded from <var>res</var> and returned.  Otherwise
     * {@link #title} is returned.
     */
    public CharSequence getTitle(Resources res) {
        if (titleRes != 0) {
            return res.getText(titleRes);
        }
        return title;
    }

    public CharSequence getSummary(Resources res) {
        if (summaryRes != 0) {
            return res.getText(summaryRes);
        }
        return summary;
    }

    public void onSummarySet(TextView view) {

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(titleRes);
        TextUtils.writeToParcel(title, dest, flags);

        final int count = tiles.size();
        dest.writeInt(count);

        for (int n = 0; n < count; n++) {
            Tile tile = tiles.get(n);
            tile.writeToParcel(dest, flags);
        }
    }

    public void readFromParcel(Parcel in) {
        titleRes = in.readInt();
        title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

        final int count = in.readInt();

        for (int n = 0; n < count; n++) {
            Tile tile = Tile.CREATOR.createFromParcel(in);
            tiles.add(tile);
        }
    }
}
