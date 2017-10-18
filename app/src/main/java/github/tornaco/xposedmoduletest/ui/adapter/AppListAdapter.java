package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.DrawableImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private Context context;

    public AppListAdapter(Context context) {
        this.context = context;
    }

    protected final List<PackageInfo> packageInfos = new ArrayList<>();

    public void update(Collection<PackageInfo> src) {
        synchronized (packageInfos) {
            packageInfos.clear();
            packageInfos.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppViewHolder(view);
    }

    public List<PackageInfo> getPackageInfos() {
        return packageInfos;
    }

    protected
    @LayoutRes
    int getTemplateLayoutRes() {
        return R.layout.data_item_template_with_checkable;
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        PackageInfo packageInfo = packageInfos.get(position);
        holder.getLineOneTextView().setText(packageInfo.getAppName());
        holder.getCheckableImageView().setChecked(false);
        holder.getLineTwoTextView().setText(String.valueOf(packageInfo.getPkgName()));
        Vangogh.with(context)
                .load(packageInfo.getPkgName())
                .usingLoader(new Loader<Image>() {
                    @Nullable
                    @Override
                    public Image load(@NonNull ImageSource source,
                                      @Nullable LoaderObserver observer) {
                        String pkgName = source.getUrl();
                        return new DrawableImage(ApkUtil.loadIconByPkgName(context, pkgName));
                    }

                    @Override
                    public int priority() {
                        return 3;
                    }

                    @Override
                    public ExecutorService getExecutor() {
                        return null;
                    }
                })
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());
    }

    @Override
    public int getItemCount() {
        return packageInfos.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView lineOneTextView;
        private TextView lineTwoTextView;
        private CheckableImageView checkableImageView;

        public AppViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = (TextView) itemView.findViewById(android.R.id.title);
            lineTwoTextView = (TextView) itemView.findViewById(android.R.id.text1);
            checkableImageView = (CheckableImageView) itemView.findViewById(R.id.checkable_img_view);
        }

        public TextView getLineOneTextView() {
            return lineOneTextView;
        }

        public TextView getLineTwoTextView() {
            return lineTwoTextView;
        }

        public CheckableImageView getCheckableImageView() {
            return checkableImageView;
        }
    }
}
