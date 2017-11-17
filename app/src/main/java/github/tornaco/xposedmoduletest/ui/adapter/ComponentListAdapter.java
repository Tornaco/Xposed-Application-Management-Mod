package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int getTemplateLayoutRes() {
        return R.layout.comp_list_item;
    }

    @Override
    public void onBindViewHolder(ComponentHolder holder, int position) {
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
        private Switch compSwitch;

        ComponentHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.text1);
            this.compSwitch = itemView.findViewById(R.id.comp_switch);
        }
    }
}
