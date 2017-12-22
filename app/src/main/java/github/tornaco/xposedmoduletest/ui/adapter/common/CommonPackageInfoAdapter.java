package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import lombok.Getter;
import lombok.Setter;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoAdapter
        extends RecyclerView.Adapter<CommonPackageInfoAdapter.CommonViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Context context;
    private VangoghAppLoader vangoghAppLoader;

    @Getter
    private int selection = -1;

    @ColorInt
    @Setter
    private int highlightColor, normalColor;

    @Setter
    @Getter
    private boolean choiceMode;

    @Setter
    @Getter
    private ChoiceModeListener choiceModeListener;

    public CommonPackageInfoAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);

        this.highlightColor = ContextCompat.getColor(context, R.color.accent);
        this.normalColor = ContextCompat.getColor(context, R.color.card);

        setChoiceMode(false);
    }

    @Getter
    final List<CommonPackageInfo> commonPackageInfos = new ArrayList<>();

    public void update(Collection<? extends CommonPackageInfo> src) {
        synchronized (commonPackageInfos) {
            commonPackageInfos.clear();
            commonPackageInfos.addAll(src);
        }
        notifyDataSetChanged();
    }

    public void selectAll(final boolean selectAll) {
        Collections.consumeRemaining(getCommonPackageInfos(), new Consumer<CommonPackageInfo>() {
            @Override
            public void accept(CommonPackageInfo packageInfo) {
                packageInfo.setChecked(selectAll);
            }
        });
        notifyDataSetChanged();
        onItemCheckChanged();
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new CommonViewHolder(view);
    }

    @LayoutRes
    protected int getTemplateLayoutRes() {
        return R.layout.app_list_item_1;
    }

    @Override
    public void onBindViewHolder(final CommonViewHolder holder, final int position) {
        final CommonPackageInfo packageInfo = commonPackageInfos.get(position);

        holder.getSystemAppIndicator().setVisibility(packageInfo.isSystemApp()
                ? View.VISIBLE : View.GONE);
        holder.getExtraIndicator().setVisibility(View.INVISIBLE);

        holder.getLineOneTextView().setText(packageInfo.getAppName());

        holder.getCheckableImageView().setChecked(false, false);
        Vangogh.with(context)
                .load(packageInfo.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(onCreateImageApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getCheckableImageView());

        if (getSelection() >= 0 && position == selection) {
            holder.itemView.setBackgroundColor(highlightColor);
        } else {
            holder.itemView.setBackgroundColor(normalColor);
        }

        if (isChoiceMode()) {
            final CommonPackageInfo commonPackageInfo = getCommonPackageInfos().get(position);
            holder.getCheckableImageView().setChecked(commonPackageInfo.isChecked(), false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commonPackageInfo.setChecked(!commonPackageInfo.isChecked());
                    holder.getCheckableImageView().setChecked(commonPackageInfo.isChecked());
                    onItemCheckChanged();
                }
            });
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemLongClick(v, holder.getAdapterPosition());
            }
        });
    }

    public interface ItemCheckListener {
        void onItemCheckChanged(int total, int checked);
    }

    @Getter
    @Setter
    private ItemCheckListener itemCheckListener;

    private void onItemCheckChanged() {
        if (itemCheckListener != null) {
            itemCheckListener.onItemCheckChanged(getItemCount(), getCheckedCount());
        }
    }

    protected ImageApplier onCreateImageApplier() {
        return new FadeOutFadeInApplier();
    }

    protected boolean onItemLongClick(View v, int position) {
        if (!isChoiceMode()) {
            setChoiceMode(true);
            choiceModeListener.onEnterChoiceMode();
            onInfoItemLongClickSelected(position);
            return true;
        }
        return false;
    }


    private void onInfoItemLongClickSelected(int position) {
        getCommonPackageInfos().get(position).setChecked(true);
        notifyDataSetChanged();
    }

    public boolean onBackPressed() {
        if (isChoiceMode()) {
            setChoiceMode(false);
            clearCheckStateForAllItems();
            notifyDataSetChanged();
            choiceModeListener.onLeaveChoiceMode();
            return true;
        }
        return false;
    }

    private void clearCheckStateForAllItems() {
        Collections.consumeRemaining(getCommonPackageInfos(),
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo info) {
                        if (info.isChecked()) info.setChecked(false);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commonPackageInfos.size();
    }

    public int getCheckedCount() {
        final int[] c = {0};
        Collections.consumeRemaining(commonPackageInfos,
                new Consumer<CommonPackageInfo>() {
                    @Override
                    public void accept(CommonPackageInfo info) {
                        if (info.isChecked()) c[0]++;
                    }
                });
        return c[0];
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String appName = getCommonPackageInfos().get(position).getAppName();
        if (appName == null
                || appName.length() < 1)
            appName = getCommonPackageInfos().get(position).getPkgName();
        return String.valueOf(appName.charAt(0));
    }

    public void setSelection(int selection) {
        this.selection = selection;
        notifyDataSetChanged();
    }

    @Getter
    public static class CommonViewHolder extends RecyclerView.ViewHolder {
        private TextView lineOneTextView, lineTwoTextView, systemAppIndicator;
        private View extraIndicator;
        private CheckableImageView checkableImageView;

        CommonViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = itemView.findViewById(android.R.id.title);
            lineTwoTextView = itemView.findViewById(android.R.id.text2);
            systemAppIndicator = itemView.findViewById(android.R.id.text1);
            checkableImageView = itemView.findViewById(R.id.checkable_img_view);
            extraIndicator = itemView.findViewById(R.id.extra_indicator);
        }
    }

    public interface ChoiceModeListener {
        void onEnterChoiceMode();

        void onLeaveChoiceMode();
    }
}
