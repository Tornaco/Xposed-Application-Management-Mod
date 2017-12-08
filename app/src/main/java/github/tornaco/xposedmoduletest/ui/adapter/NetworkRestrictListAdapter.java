package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.model.NetworkRestrictionItem;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import lombok.Getter;
import lombok.Setter;

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
    private VangoghAppLoader vangoghAppLoader;

    public NetworkRestrictListAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);
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

    @Override
    public void onBindViewHolder(RestrictItemHolder holder, int position) {
        final NetworkRestrictionItem item = getNetworkRestrictionItems().get(position);
        holder.getTitleView().setText(item.getAppName());

        if (isRestrictWifi()) {
            holder.getRestrictSwitch().setChecked(!item.isRestrictedWifi());
        } else {
            holder.getRestrictSwitch().setChecked(!item.isRestrictedData());
        }

        holder.getSummaryView().setVisibility(item.isSystemApp() ? View.VISIBLE : View.GONE);

        if (XAshmanManager.get().isServiceAvailable()) {

            holder.getRestrictSwitch().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch s = (Switch) v;
                    if (isRestrictWifi()) {
                        XAshmanManager.get().restrictAppOnWifi(item.getUid(), !s.isChecked());
                    } else {
                        XAshmanManager.get().restrictAppOnData(item.getUid(), !s.isChecked());
                    }
                }
            });
        }

        Vangogh.with(context)
                .load(item.getPackageName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getIconView());
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

    @Getter
    public final class RestrictItemHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView titleView;
        private TextView summaryView;
        private Switch restrictSwitch;

        RestrictItemHolder(View itemView) {
            super(itemView);

            this.iconView = itemView.findViewById(R.id.icon);
            this.titleView = itemView.findViewById(R.id.title);
            this.summaryView = itemView.findViewById(R.id.status);
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
