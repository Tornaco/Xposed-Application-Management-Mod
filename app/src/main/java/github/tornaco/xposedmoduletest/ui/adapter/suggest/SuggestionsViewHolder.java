package github.tornaco.xposedmoduletest.ui.adapter.suggest;

import android.view.View;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import github.tornaco.xposedmoduletest.R;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/3/14.
 */
@Getter
public class SuggestionsViewHolder extends GroupViewHolder {

    private TextView suggestionsTitleView;
    private TextView badgeView;

    public SuggestionsViewHolder(View itemView) {
        super(itemView);
        suggestionsTitleView = itemView.findViewById(R.id.suggestions_title);
        badgeView = itemView.findViewById(R.id.badge);
    }
}
