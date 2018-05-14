package github.tornaco.xposedmoduletest.ui.adapter.suggest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.Suggestion;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/3/14.
 */

public class SuggestionsAdapter extends ExpandableRecyclerViewAdapter<SuggestionsViewHolder, SuggestionViewHolder> {

    @Getter
    private Context context;

    public SuggestionsAdapter(Context context, List<? extends ExpandableGroup> groups) {
        super(groups);
        this.context = context;
    }

    @Override
    public SuggestionsViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return new SuggestionsViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_suggestions_item, parent, false));
    }

    @Override
    public SuggestionViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        return new SuggestionViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_suggestion_item, parent, false));
    }

    @Override
    public void onBindChildViewHolder(final SuggestionViewHolder holder, final int flatPosition,
                                      final ExpandableGroup group, final int childIndex) {
        final Suggestion suggestion = (Suggestion) group.getItems().get(childIndex);
        holder.getTitleView().setText(suggestion.getTitle());
        holder.getSummaryView().setText(suggestion.getSummary());
        holder.getIconView().setImageResource(suggestion.getIconRes());
        holder.getActionView().setText(suggestion.getActionLabel());
        holder.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (suggestion.getOnActionClickListener().onActionClick(group, flatPosition, childIndex)) {
                    group.getItems().remove(suggestion);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onBindGroupViewHolder(SuggestionsViewHolder holder, int flatPosition, ExpandableGroup group) {
        String title = group.getTitle();
        holder.getSuggestionsTitleView().setText(title);
        holder.getBadgeView().setText(String.format("+%s", group.getItems().size()));
    }

    public interface OnExpandableGroupActionClickListener {
        boolean onActionClick(ExpandableGroup group, int flatPosition, int childIndex);
    }
}
