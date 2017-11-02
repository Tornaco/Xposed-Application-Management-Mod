package dev.nick.tiles.tile;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckboxTileView extends TileView implements Checkable {

    protected CheckBox mBox;

    public CheckboxTileView(Context context) {
        super(context);
    }

    public CheckboxTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindActionView(RelativeLayout container) {
        final CheckBox s = new CheckBox(getContext());
        s.setSoundEffectsEnabled(false);
        container.addView(s, generateCenterParams());
        s.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = s.isChecked();
                onCheckChanged(checked);
            }
        });
        mBox = s;
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
        mBox.performClick();
    }

    @Override
    public boolean isChecked() {
        ensureSwitch();
        return mBox.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        ensureSwitch();
        mBox.setChecked(checked);
    }

    @Override
    public void toggle() {
        ensureSwitch();
        mBox.setChecked(!mBox.isChecked());
    }

    private void ensureSwitch() {
        if (mBox == null) {
            throw new IllegalStateException("View not finished inflate yet.");
        }
    }

    static class SavedState extends BaseSavedState {
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
