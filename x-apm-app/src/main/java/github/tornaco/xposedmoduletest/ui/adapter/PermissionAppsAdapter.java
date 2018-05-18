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
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Getter;
import lombok.Setter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */

public class PermissionAppsAdapter extends RecyclerView.Adapter<PermissionAppsAdapter.AppsHolder> {

    @Getter
    private final List<CommonPackageInfo> data = Lists.newArrayList();

    @Getter
    private int selection = -1;

    private Context context;

    @ColorInt
    @Setter
    private int highlightColor, normalColor;

    @Setter
    @Getter
    private int op;

    public PermissionAppsAdapter(Context context) {
        this.context = context;
        this.highlightColor = ContextCompat.getColor(context, R.color.blue_grey);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;

        this.normalColor = ContextCompat.getColor(context, resId);
    }

    public void update(Collection<CommonPackageInfo> src) {
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
    public AppsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppsHolder(view);
    }

    @LayoutRes
    int getTemplateLayoutRes() {
        return R.layout.perm_list_item_apps;
    }

    @Override
    public void onBindViewHolder(AppsHolder holder, int position) {
        final CommonPackageInfo commonPackageInfo = getData().get(position);

        holder.getTitleView().setText(commonPackageInfo.getAppName());

        if (commonPackageInfo.isSystemApp()) {
            holder.getSummaryView().setVisibility(View.VISIBLE);
        } else {
            holder.getSummaryView().setVisibility(View.GONE);
        }

        GlideApp.with(context)
                .load(commonPackageInfo)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .transition(withCrossFade())
                .into(holder.getIconView());


        boolean allowed = commonPackageInfo.getVersion() == XAppOpsManager.MODE_ALLOWED;
        holder.getCompSwitch().setChecked(allowed);

        holder.getCompSwitch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checkable c = (Checkable) v;
                int mode = c.isChecked() ? XAppOpsManager.MODE_ALLOWED : XAppOpsManager.MODE_IGNORED;
                XAPMManager.get().setPermissionControlBlockModeForPkg(getOp(),
                        commonPackageInfo.getPkgName(), mode);
                commonPackageInfo.setVersion(mode);
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
    static final class AppsHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView titleView;
        private TextView summaryView;
        private Switch compSwitch;

        AppsHolder(View itemView) {
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
