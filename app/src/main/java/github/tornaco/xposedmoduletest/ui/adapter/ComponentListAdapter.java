package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */
@Getter
public class ComponentListAdapter<T>
        extends RecyclerView.Adapter<ComponentListAdapter.ComponentHolder> {

    private final List<T> data = Lists.newArrayList();

    private Context context;

    public ComponentListAdapter(Context context) {
        this.context = context;
    }

    public void update(Collection<T> src) {
        synchronized (data) {
            data.clear();
            data.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public ComponentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new ComponentHolder(view);
    }

    @LayoutRes
    int getTemplateLayoutRes() {
        return R.layout.comp_list_item;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
        final T t = getData().get(position);
        holder.getMoreIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopMenu(t, v);
            }
        });
    }

    protected void showPopMenu(final T t, View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.inflate(getPopupMenuRes());
        popupMenu.setOnMenuItemClickListener(onCreateOnMenuItemClickListener(t));
        popupMenu.show();
    }

    protected int getPopupMenuRes() {
        return R.menu.component_item;
    }

    protected PopupMenu.OnMenuItemClickListener onCreateOnMenuItemClickListener(T t) {
        throw new RuntimeException("No impl");
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
        private Switch compSwitch;
        private ImageButton moreIcon;

        ComponentHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(R.id.title);
            this.summaryView = itemView.findViewById(R.id.status);
            this.summaryView2 = itemView.findViewById(R.id.status2);
            this.compSwitch = itemView.findViewById(R.id.comp_switch);
            this.moreIcon = itemView.findViewById(R.id.more);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compSwitch.performClick();
                }
            });
        }
    }
}
