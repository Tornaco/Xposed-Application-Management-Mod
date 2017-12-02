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
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.provider.BootPackageProvider;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class BootAppListAdapter extends RecyclerView.Adapter<BootAppListAdapter.AppViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Context context;
    private VangoghAppLoader vangoghAppLoader;

    private CircleImageEffect circleImageEffect = new CircleImageEffect();

    public BootAppListAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);
    }

    final List<BootCompletePackage> BootCompletePackages = new ArrayList<>();

    public void update(Collection<BootCompletePackage> src) {
        synchronized (BootCompletePackages) {
            BootCompletePackages.clear();
            BootCompletePackages.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppViewHolder(view);
    }

    public List<BootCompletePackage> getBootCompletePackages() {
        return BootCompletePackages;
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.app_list_item_1;
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final BootCompletePackage BootCompletePackage = BootCompletePackages.get(position);
        holder.getLineOneTextView().setText(BootCompletePackage.getAppName());
        holder.getCheckableImageView().setChecked(false);
        Vangogh.with(context)
                .load(BootCompletePackage.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removePkgAsync(BootCompletePackage);
                return true;
            }
        });
    }

    private void removePkgAsync(BootCompletePackage pkg) {
        BootPackageProvider.delete(context, pkg);
        onPackageRemoved(pkg.getPkgName());
    }

    protected void onPackageRemoved(String pkg) {

    }

    @Override
    public int getItemCount() {
        return BootCompletePackages.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String appName = getBootCompletePackages().get(position).getAppName();
        if (appName == null
                || appName.length() < 1)
            appName = getBootCompletePackages().get(position).getPkgName();
        return String.valueOf(appName.charAt(0));
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView lineOneTextView;
        private CheckableImageView checkableImageView;

        AppViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = itemView.findViewById(android.R.id.title);
            checkableImageView = itemView.findViewById(R.id.checkable_img_view);
        }

        TextView getLineOneTextView() {
            return lineOneTextView;
        }

        CheckableImageView getCheckableImageView() {
            return checkableImageView;
        }
    }
}
