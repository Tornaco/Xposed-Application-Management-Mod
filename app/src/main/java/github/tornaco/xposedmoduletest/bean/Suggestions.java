package github.tornaco.xposedmoduletest.bean;

import android.os.Parcel;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by Tornaco on 2018/3/14.
 */

public class Suggestions extends ExpandableGroup<Suggestion> {

    public Suggestions(String title, List<Suggestion> items) {
        super(title, items);
    }

    protected Suggestions(Parcel in) {
        super(in);
    }
}
