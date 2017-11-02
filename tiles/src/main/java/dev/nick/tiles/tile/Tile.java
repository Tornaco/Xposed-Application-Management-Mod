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

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Description of a single dashboard tile that the user can select.
 */
public class Tile {
    /**
     * Default value for {@link Tile#id DashboardTile.id}
     * indicating that no identifier value is set.  All other values (including those below -1)
     * are valid.
     */
    public static final int TILE_ID_UNDEFINED = -1;
    public static final Parcelable.Creator<Tile> CREATOR = new Parcelable.Creator<Tile>() {
        public Tile createFromParcel(Parcel source) {
            return new Tile(source);
        }

        public Tile[] newArray(int size) {
            return new Tile[size];
        }
    };
    /**
     * Identifier for this tile, to correlate with a new list when
     * it is updated.  The default value is
     * {@link Tile#TILE_ID_UNDEFINED}, meaning no id.
     *
     * @attr ref android.R.styleable#PreferenceHeader_id
     */
    public int id = TILE_ID_UNDEFINED;
    /**
     * Resource ID of title of the tile that is shown to the user.
     *
     * @attr ref android.R.styleable#PreferenceHeader_title
     */
    public int titleRes;
    /**
     * Title of the tile that is shown to the user.
     *
     * @attr ref android.R.styleable#PreferenceHeader_title
     */
    public CharSequence title;
    /**
     * Resource ID of optional summary describing what this tile controls.
     *
     * @attr ref android.R.styleable#PreferenceHeader_summary
     */
    public int summaryRes;
    /**
     * Optional summary describing what this tile controls.
     *
     * @attr ref android.R.styleable#PreferenceHeader_summary
     */
    public CharSequence summary;
    /**
     * Optional icon resource to show for this tile.
     *
     * @attr ref android.R.styleable#PreferenceHeader_icon
     */
    public int iconRes;

    public Drawable iconDrawable;

    /**
     * Full class name of the fragment to display when this tile is
     * selected.
     *
     * @attr ref android.R.styleable#PreferenceHeader_fragment
     */
    public String fragment;
    /**
     * Optional arguments to supply to the fragment when it is
     * instantiated.
     */
    public Bundle fragmentArguments;
    /**
     * Intent to launch when the preference is selected.
     */
    public Intent intent;
    /**
     * Optional additional data for use by subclasses of the activity
     */
    public Bundle extras;
    public TileView tileView;

    public Tile() {
        // Empty
    }

    Tile(Parcel in) {
        readFromParcel(in);
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

    public TileView getTileView() {
        return tileView;
    }

    /**
     * Return the currently set summary.  If {@link #summaryRes} is set,
     * this resource is loaded from <var>res</var> and returned.  Otherwise
     * {@link #summary} is returned.
     */
    public CharSequence getSummary(Resources res) {
        if (summaryRes != 0) {
            return res.getText(summaryRes);
        }
        return summary;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(titleRes);
        TextUtils.writeToParcel(title, dest, flags);
        dest.writeInt(summaryRes);
        TextUtils.writeToParcel(summary, dest, flags);
        dest.writeInt(iconRes);
        dest.writeString(fragment);
        dest.writeBundle(fragmentArguments);
        if (intent != null) {
            dest.writeInt(1);
            intent.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeBundle(extras);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        titleRes = in.readInt();
        title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        summaryRes = in.readInt();
        summary = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        iconRes = in.readInt();
        fragment = in.readString();
        fragmentArguments = in.readBundle();
        if (in.readInt() != 0) {
            intent = Intent.CREATOR.createFromParcel(in);
        }
        extras = in.readBundle();
    }
}
