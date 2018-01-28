package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import lombok.Getter;
import lombok.Setter;
import tornaco.lib.widget.CheckableImageView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoAdapter
        extends RecyclerView.Adapter<CommonPackageInfoAdapter.CommonViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    @Getter
    private Context context;

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
        this.highlightColor = ContextCompat.getColor(context, R.color.accent);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;

        this.normalColor = ContextCompat.getColor(context, resId);

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
        return onCreateViewHolder(view);
    }

    protected CommonViewHolder onCreateViewHolder(View root) {
        return new CommonViewHolder(root);
    }

    @LayoutRes
    protected int getTemplateLayoutRes() {
        return R.layout.app_list_item_1;
    }

    protected String getSystemAppIndicatorLabel(int appLevel) {

        switch (appLevel) {
            case XAshmanManager.AppLevel.THIRD_PARTY:
                return null;
            case XAshmanManager.AppLevel.SYSTEM:
                return context.getString(R.string.app_level_system);
            case XAshmanManager.AppLevel.SYSTEM_UID:
                return context.getString(R.string.app_level_core);
            case XAshmanManager.AppLevel.PHONE_UID:
                return context.getString(R.string.app_level_phone);
            case XAshmanManager.AppLevel.MEDIA_UID:
                return context.getString(R.string.app_level_media);
        }
        return null;
    }

    protected int getSystemAppIndicatorColor(int appLevel) {
        switch (appLevel) {
            case XAshmanManager.AppLevel.THIRD_PARTY:
                return 0;
            case XAshmanManager.AppLevel.SYSTEM:
                return ContextCompat.getColor(context, R.color.amber_dark);
            case XAshmanManager.AppLevel.SYSTEM_UID:
                return ContextCompat.getColor(context, R.color.red_dark);
            case XAshmanManager.AppLevel.PHONE_UID:
                return ContextCompat.getColor(context, R.color.green_dark);
            case XAshmanManager.AppLevel.MEDIA_UID:
                return ContextCompat.getColor(context, R.color.blue_dark);
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(final CommonViewHolder holder, final int position) {
        final CommonPackageInfo packageInfo = commonPackageInfos.get(position);

        int appLevel = packageInfo.getAppLevel();

        holder.getSystemAppIndicator().setVisibility(appLevel != XAshmanManager.AppLevel.THIRD_PARTY ? View.VISIBLE : View.GONE);
        holder.getSystemAppIndicator().setText(getSystemAppIndicatorLabel(appLevel));
        int levelColor = getSystemAppIndicatorColor(appLevel);
        holder.getSystemAppIndicator().setTextColor(levelColor);

        holder.getExtraIndicator().setVisibility(View.INVISIBLE);

        holder.getLineOneTextView().setText(packageInfo.getAppName());

        holder.getCheckableImageView().setChecked(false, false);

        if (imageLoadingEnabled()) {
            GlideApp.with(context)
                    .load(packageInfo)
                    .placeholder(0)
                    .error(R.mipmap.ic_launcher_round)
                    .fallback(R.mipmap.ic_launcher_round)
                    .transition(withCrossFade())
                    .into(holder.getCheckableImageView());
        }

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
                    if (isChoiceMode()) {
                        commonPackageInfo.setChecked(!commonPackageInfo.isChecked());
                        holder.getCheckableImageView().setChecked(commonPackageInfo.isChecked());
                        onItemCheckChanged();
                    }
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

    protected boolean imageLoadingEnabled() {
        return true;
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

        public CommonViewHolder(View itemView) {
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
