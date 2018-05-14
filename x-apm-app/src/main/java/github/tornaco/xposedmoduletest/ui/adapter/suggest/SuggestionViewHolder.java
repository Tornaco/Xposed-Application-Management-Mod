package github.tornaco.xposedmoduletest.ui.adapter.suggest;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import github.tornaco.xposedmoduletest.R;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/3/14.
 */
@Getter
public class SuggestionViewHolder extends ChildViewHolder {

    private TextView titleView, summaryView;
    private Button actionView;
    private ImageView iconView;

    public SuggestionViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title);
        summaryView = itemView.findViewById(R.id.summary);
        iconView = itemView.findViewById(R.id.icon);
        actionView = itemView.findViewById(R.id.suggestion_action);
    }
}
