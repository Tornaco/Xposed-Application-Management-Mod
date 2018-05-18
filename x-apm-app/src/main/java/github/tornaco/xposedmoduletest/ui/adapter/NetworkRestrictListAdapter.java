package github.tornaco.xposedmoduletest.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.model.NetworkRestrictionItem;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Getter;
import lombok.Setter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by guohao4 on 2017/12/5.
 * Email: Tornaco@163.com
 */

public class NetworkRestrictListAdapter
        extends RecyclerView.Adapter<NetworkRestrictListAdapter.RestrictItemHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    @Getter
    @Setter
    private boolean restrictWifi, restrictData;

    private Context context;

    @Getter
    private int selection = -1;

    @ColorInt
    @Setter
    private int highlightColor, normalColor;

    public NetworkRestrictListAdapter(Context context) {
        this.context = context;

        this.highlightColor = ContextCompat.getColor(context, R.color.blue_grey);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;

        this.normalColor = ContextCompat.getColor(context, resId);
    }

    @Getter
    final List<NetworkRestrictionItem> networkRestrictionItems = new ArrayList<>();

    public void update(Collection<NetworkRestrictionItem> src) {
        synchronized (networkRestrictionItems) {
            networkRestrictionItems.clear();
            networkRestrictionItems.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public RestrictItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new RestrictItemHolder(view);
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.network_restrict_item;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RestrictItemHolder holder, int position) {
        final NetworkRestrictionItem item = getNetworkRestrictionItems().get(position);
        holder.getTitleView().setText(item.getAppName());
        holder.getSummaryView().setText("UID: " + item.getUid());

        if (isRestrictWifi()) {
            holder.getRestrictSwitch().setChecked(!item.isRestrictedWifi());
        } else {
            holder.getRestrictSwitch().setChecked(!item.isRestrictedData());
        }

        holder.getSystemAppIndicator().setVisibility(item.isSystemApp() ? View.VISIBLE : View.GONE);

        if (XAPMManager.get().isServiceAvailable()) {

            holder.getRestrictSwitch().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch s = (Switch) v;
                    if (isRestrictWifi()) {
                        XAPMManager.get().restrictAppOnWifi(item.getUid(), !s.isChecked());
                    } else {
                        XAPMManager.get().restrictAppOnData(item.getUid(), !s.isChecked());
                    }
                }
            });
        }

        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(item.getPackageName());
        GlideApp.with(context)
                .load(c)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .transition(withCrossFade())
                .into(holder.iconView);

        if (getSelection() >= 0 && position == selection) {
            holder.itemView.setBackgroundColor(highlightColor);
        } else {
            holder.itemView.setBackgroundColor(normalColor);
        }
    }

    @Override
    public int getItemCount() {
        return getNetworkRestrictionItems().size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final NetworkRestrictionItem item = getNetworkRestrictionItems().get(position);
        if (TextUtils.isEmpty(item.getAppName())) return "?";
        return String.valueOf(item.getAppName().charAt(0));
    }

    public void setSelection(int selection) {
        this.selection = selection;
        notifyDataSetChanged();
    }

    @Getter
    public final class RestrictItemHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView titleView;
        private TextView summaryView;
        private TextView systemAppIndicator;
        private Switch restrictSwitch;

        RestrictItemHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.text2);
            this.systemAppIndicator = itemView.findViewById(android.R.id.text1);
            this.restrictSwitch = itemView.findViewById(R.id.restrict_switch);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restrictSwitch.performClick();
                }
            });
        }
    }
}
