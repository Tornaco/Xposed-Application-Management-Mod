package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.CircleImageEffect;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import lombok.Getter;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class LockKillAppListAdapter extends RecyclerView.Adapter<LockKillAppListAdapter.AppViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Context context;
    private VangoghAppLoader vangoghAppLoader;

    private CircleImageEffect circleImageEffect = new CircleImageEffect();

    public LockKillAppListAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);
    }

    final List<LockKillPackage> LockKillPackages = new ArrayList<>();

    public void update(Collection<LockKillPackage> src) {
        synchronized (LockKillPackages) {
            LockKillPackages.clear();
            LockKillPackages.addAll(src);
        }
        notifyDataSetChanged();
    }

    public void selectAll(final boolean select) {
        Collections.consumeRemaining(getLockKillPackages(), new Consumer<LockKillPackage>() {
            @Override
            public void accept(LockKillPackage lockKillPackage) {
                lockKillPackage.setKill(!select);
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppViewHolder(view);
    }

    public List<LockKillPackage> getLockKillPackages() {
        return LockKillPackages;
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.app_list_item_1;
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final LockKillPackage killPackage = LockKillPackages.get(position);
        holder.getLineOneTextView().setText(killPackage.getAppName());
        holder.getSystemAppIndicator().setVisibility(killPackage.isSystemApp()
                ? View.VISIBLE : View.GONE);
        holder.getCheckableImageView().setChecked(false);
        Vangogh.with(context)
                .load(killPackage.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removePkgAsync(killPackage);
                return true;
            }
        });
    }

    private void removePkgAsync(LockKillPackage pkg) {
        XAshmanManager.singleInstance()
                .addOrRemoveLKApps(new String[]{pkg.getPkgName()},
                        XAshmanManager.Op.REMOVE);
        onPackageRemoved(pkg.getPkgName());
    }

    protected void onPackageRemoved(String pkg) {

    }

    @Override
    public int getItemCount() {
        return LockKillPackages.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String appName = getLockKillPackages().get(position).getAppName();
        if (appName == null
                || appName.length() < 1)
            appName = getLockKillPackages().get(position).getPkgName();
        return String.valueOf(appName.charAt(0));
    }

    @Getter
    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView lineOneTextView, systemAppIndicator;
        private CheckableImageView checkableImageView;

        AppViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = itemView.findViewById(android.R.id.title);
            systemAppIndicator = itemView.findViewById(android.R.id.text1);
            checkableImageView = itemView.findViewById(R.id.checkable_img_view);
        }
    }
}
