package github.tornaco.xposedmoduletest.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import github.tornaco.xposedmoduletest.ui.adapter.suggest.SuggestionsAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/3/14.
 */
@Getter
@AllArgsConstructor
public class Suggestion implements Parcelable {

    private String title, summary, actionLabel;
    @DrawableRes
    private int iconRes;
    private SuggestionsAdapter.OnExpandableGroupActionClickListener onActionClickListener;

    private Suggestion(Parcel in) {
        title = in.readString();
        summary = in.readString();
        actionLabel = in.readString();
        iconRes = in.readInt();
    }

    public static final Creator<Suggestion> CREATOR = new Creator<Suggestion>() {
        @Override
        public Suggestion createFromParcel(Parcel in) {
            return new Suggestion(in);
        }

        @Override
        public Suggestion[] newArray(int size) {
            return new Suggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(actionLabel);
        dest.writeInt(iconRes);
    }
}
