package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
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

    final List<PackageInfo> packageInfos = new ArrayList<>();

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

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.data_item_template_with_checkable;
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final PackageInfo packageInfo = packageInfos.get(position);
        holder.getLineOneTextView().setText(packageInfo.getAppName());
        holder.getCheckableImageView().setChecked(false);
        holder.getLineTwoTextView().setText(String.valueOf(packageInfo.getPkgName()));
        Vangogh.with(context)
                .load(packageInfo.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(new Loader<Image>() {
                    @Nullable
                    @Override
                    public Image load(@NonNull ImageSource source,
                                      @Nullable LoaderObserver observer) {
                        String pkgName = source.getUrl();
                        Drawable d = ApkUtil.loadIconByPkgName(context, pkgName);
                        BitmapDrawable bd = (BitmapDrawable) d;
                        Logger.v("XXX- Loading COMPLETE for: " + pkgName);
                        BitmapImage bitmapImage = new BitmapImage(bd.getBitmap());
                        if (observer != null) {
                            observer.onImageReady(bitmapImage);
                        }
                        return bitmapImage;
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
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removePkgAsync(packageInfo);
                return true;
            }
        });
    }

    private void removePkgAsync(PackageInfo pkg) {
        DaoSession d = DaoManager.getInstance().getSession(context);
        if (d == null) return;
        d.getPackageInfoDao().delete(pkg);
        onPackageRemoved();
    }

    protected void onPackageRemoved() {

    }

    @Override
    public int getItemCount() {
        return packageInfos.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView lineOneTextView;
        private TextView lineTwoTextView;
        private CheckableImageView checkableImageView;

        AppViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = (TextView) itemView.findViewById(android.R.id.title);
            lineTwoTextView = (TextView) itemView.findViewById(android.R.id.text1);
            checkableImageView = (CheckableImageView) itemView.findViewById(R.id.checkable_img_view);
        }

        TextView getLineOneTextView() {
            return lineOneTextView;
        }

        TextView getLineTwoTextView() {
            return lineTwoTextView;
        }

        CheckableImageView getCheckableImageView() {
            return checkableImageView;
        }
    }
}
