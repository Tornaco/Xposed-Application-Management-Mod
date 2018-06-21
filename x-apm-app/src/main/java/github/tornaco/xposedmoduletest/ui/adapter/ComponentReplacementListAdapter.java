package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */
@Getter
public class ComponentReplacementListAdapter
        extends RecyclerView.Adapter<ComponentReplacementListAdapter.ComponentHolder> {

    private final List<ComponentReplacement> data = Lists.newArrayList();

    @Getter
    private int selection = -1;

    private Context context;

    @ColorInt
    @Setter
    private int highlightColor, normalColor;

    public ComponentReplacementListAdapter(Context context) {
        this.context = context;
        this.highlightColor = ContextCompat.getColor(context, R.color.blue_grey);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;

        this.normalColor = ContextCompat.getColor(context, resId);
    }

    public void update(Collection<ComponentReplacement> src) {
        synchronized (data) {
            data.clear();
            data.addAll(src);
        }
        notifyDataSetChanged();
    }

    public void setSelection(int selection) {
        this.selection = selection;
        notifyDataSetChanged();
    }

    @Override
    public ComponentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new ComponentHolder(view);
    }

    @LayoutRes
    int getTemplateLayoutRes() {
        return R.layout.comp_replacement_list_item;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        final ComponentReplacement t = getData().get(position);
        holder.getMoreIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopMenu(t, v);
            }
        });
        if (getSelection() >= 0 && position == selection) {
            holder.itemView.setBackgroundColor(highlightColor);
        } else {
            holder.itemView.setBackgroundColor(normalColor);
        }

        holder.getTitleView().setText(t.getAppName());
        holder.getSummaryView().setText(t.fromFlattenToString());
        holder.getSummaryView2().setText(t.toFlattenToString());


        holder.getIconView().setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_comp_replacement));
    }

    protected void showPopMenu(final ComponentReplacement t, View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(getPopupMenuRes());
        popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener(t));
        popupMenu.show();
    }

    protected int getPopupMenuRes() {
        return R.menu.component_replacement_item;
    }

    protected PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener(ComponentReplacement t) {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        };
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Getter
    static final class ComponentHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView titleView;
        private TextView summaryView;
        private TextView summaryView2;
        private ImageButton moreIcon;

        ComponentHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(R.id.title);
            this.summaryView = itemView.findViewById(R.id.status);
            this.summaryView2 = itemView.findViewById(R.id.status2);
            this.moreIcon = itemView.findViewById(R.id.more);

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreIcon.performClick();
                }
            });
        }
    }
}
