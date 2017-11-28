package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.CircleImageEffect;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.provider.AutoStartPackageProvider;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class StartAppListAdapter extends RecyclerView.Adapter<StartAppListAdapter.AppViewHolder>
        implements SectionIndexer {

    private Context context;
    private VangoghAppLoader vangoghAppLoader;

    private CircleImageEffect circleImageEffect = new CircleImageEffect();

    public StartAppListAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);
    }

    final List<AutoStartPackage> autoStartPackages = new ArrayList<>();

    public void update(Collection<AutoStartPackage> src) {
        synchronized (autoStartPackages) {
            autoStartPackages.clear();
            autoStartPackages.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppViewHolder(view);
    }

    public List<AutoStartPackage> getAutoStartPackages() {
        return autoStartPackages;
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.app_list_item;
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final AutoStartPackage autoStartPackage = autoStartPackages.get(position);
        holder.getLineOneTextView().setText(autoStartPackage.getAppName());
        holder.getCheckableImageView().setChecked(false);
        holder.getLineTwoTextView().setText(String.valueOf(autoStartPackage.getPkgName()));
        Vangogh.with(context)
                .load(autoStartPackage.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removePkgAsync(autoStartPackage);
                return true;
            }
        });
    }

    private void removePkgAsync(AutoStartPackage pkg) {
        AutoStartPackageProvider.delete(context, pkg);
        onPackageRemoved(pkg.getPkgName());
    }

    protected void onPackageRemoved(String pkg) {

    }

    @Override
    public int getItemCount() {
        return autoStartPackages.size();
    }

    // For index.
    private ArrayList<Integer> mSectionPositions;
    // For index end.

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = autoStartPackages.size(); i < size; i++) {
            String appName = String.valueOf(autoStartPackages.get(i).getAppName());
            String section = "";
            // FIXME Session ret.
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
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
