package dev.nick.tiles.tile;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by nick on 11/12/15.
 */
public class SwitchTileView extends TileView implements Checkable {

    private SwitchCompat mSwitch;

    public SwitchTileView(Context context) {
        super(context);
    }

    public SwitchTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindActionView(RelativeLayout container) {
        final SwitchCompat s = new SwitchCompat(getContext());
        s.setSoundEffectsEnabled(false);
        container.addView(s, generateCenterParams());
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = s.isChecked();
                onCheckChanged(checked);
            }
        });
        mSwitch = s;
    }

    protected void onCheckChanged(boolean checked) {
        // Empty.
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState myState = new SavedState(superState);
        myState.checked = isChecked();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.checked);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        mSwitch.performClick();
    }

    @Override
    public boolean isChecked() {
        ensureSwitch();
        return mSwitch.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        ensureSwitch();
        mSwitch.setChecked(checked);
    }

    @Override
    public void toggle() {
        ensureSwitch();
        mSwitch.setChecked(!mSwitch.isChecked());
    }

    private void ensureSwitch() {
        if (mSwitch == null) {
            throw new IllegalStateException("View not finished inflate yet.");
        }
    }

    static class SavedState extends View.BaseSavedState {
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        boolean checked;

        public SavedState(Parcel source) {
            super(source);
            checked = source.readInt() == 1;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(checked ? 1 : 0);
        }
    }
}
