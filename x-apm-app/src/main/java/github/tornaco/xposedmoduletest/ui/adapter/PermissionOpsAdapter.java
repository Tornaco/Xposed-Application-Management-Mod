package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class PermissionOpsAdapter extends RecyclerView.Adapter<PermissionOpsAdapter.PermissionHolder> {

    @Getter
    private final List<Permission> data = Lists.newArrayList();

    @Getter
    private int selection = -1;

    private Context context;

    @ColorInt
    @Setter
    private int highlightColor, normalColor;

    public PermissionOpsAdapter(Context context) {
        this.context = context;
        this.highlightColor = ContextCompat.getColor(context, R.color.blue_grey);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;

        this.normalColor = ContextCompat.getColor(context, resId);
    }

    public void update(Collection<Permission> src) {
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
    public PermissionOpsAdapter.PermissionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new PermissionHolder(view);
    }

    @LayoutRes
    int getTemplateLayoutRes() {
        return R.layout.perm_list_item;
    }

    @Override
    public void onBindViewHolder(PermissionOpsAdapter.PermissionHolder holder, int position) {
        final Permission permission = getData().get(position);

        int iconRes = permission.getIconRes();
        holder.getIconView().setImageResource(iconRes);
        holder.getTitleView().setText(permission.getName());
        holder.getSummaryView().setText(
                permission.getSummary());

        boolean block = permission.getMode() == XAppOpsManager.MODE_IGNORED;
        holder.getCompSwitch().setChecked(!block);

        holder.getCompSwitch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checkable c = (Checkable) v;
                int mode = c.isChecked() ? XAppOpsManager.MODE_ALLOWED : XAppOpsManager.MODE_IGNORED;
                XAPMManager.get().setPermissionControlBlockModeForPkg(permission.getCode(),
                        permission.getPkgName(), mode);
                permission.setMode(mode);
            }
        });

        if (getSelection() >= 0 && position == selection) {
            holder.itemView.setBackgroundColor(highlightColor);
        } else {
            holder.itemView.setBackgroundColor(normalColor);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Getter
    static final class PermissionHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView titleView;
        private TextView summaryView;
        private Switch compSwitch;

        PermissionHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(R.id.title);
            this.summaryView = itemView.findViewById(R.id.status);
            this.compSwitch = itemView.findViewById(R.id.comp_switch);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compSwitch.performClick();
                }
            });
        }
    }
}
