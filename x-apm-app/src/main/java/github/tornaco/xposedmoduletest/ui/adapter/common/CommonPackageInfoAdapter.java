package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.nekocode.badge.BadgeDrawable;
import github.tornaco.android.common.Collections;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
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

    private int mDefaultTextColor;

    @Setter
    @Getter
    private boolean choiceMode;

    @Setter
    @Getter
    private ChoiceModeListener choiceModeListener;

    private boolean showGcmIndicator = false;

    public CommonPackageInfoAdapter(Context context) {
        this.context = context;
        this.highlightColor = ContextCompat.getColor(context, R.color.accent);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.torCardBackgroundColor, typedValue, true);
        int resId = typedValue.resourceId;
        this.normalColor = ContextCompat.getColor(context, resId);

        context.getTheme().resolveAttribute(R.attr.torListItemTitleTextColor, typedValue, true);
        resId = typedValue.resourceId;
        this.mDefaultTextColor = ContextCompat.getColor(context, resId);

        setChoiceMode(false);

        this.showGcmIndicator = AppSettings.isShowGcmIndicator(context);
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
        Collections.consumeRemaining(getCommonPackageInfos(), packageInfo -> packageInfo.setChecked(selectAll));
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
            case XAPMManager.AppLevel.THIRD_PARTY:
                return null;
            case XAPMManager.AppLevel.SYSTEM:
                return context.getString(R.string.app_level_system);
            case XAPMManager.AppLevel.SYSTEM_UID:
                return context.getString(R.string.app_level_core);
            case XAPMManager.AppLevel.PHONE_UID:
                return context.getString(R.string.app_level_phone);
            case XAPMManager.AppLevel.MEDIA_UID:
                return context.getString(R.string.app_level_media);
            case XAPMManager.AppLevel.WEBVIEW_IMPL:
                return context.getString(R.string.app_level_webview_impl);
        }
        return "";
    }

    protected int getSystemAppIndicatorColor(int appLevel) {
        switch (appLevel) {
            case XAPMManager.AppLevel.THIRD_PARTY:
                return 0;
            case XAPMManager.AppLevel.SYSTEM:
                return ContextCompat.getColor(context, R.color.amber_dark);
            case XAPMManager.AppLevel.SYSTEM_UID:
                return ContextCompat.getColor(context, R.color.red_dark);
            case XAPMManager.AppLevel.PHONE_UID:
                return ContextCompat.getColor(context, R.color.green_dark);
            case XAPMManager.AppLevel.MEDIA_UID:
                return ContextCompat.getColor(context, R.color.blue_dark);
            case XAPMManager.AppLevel.WEBVIEW_IMPL:
                return ContextCompat.getColor(context, R.color.blue_grey);
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(final CommonViewHolder holder, final int position) {
        final CommonPackageInfo packageInfo = commonPackageInfos.get(position);

        inflatePackageDesc(packageInfo, holder.getSystemAppIndicator(), showGcmIndicator);

        holder.getExtraIndicator().setVisibility(View.INVISIBLE);

        boolean disabled = packageInfo.isDisabled();
        if (disabled) {
            holder.getLineOneTextView().setText(getContext().getString(R.string.title_app_disabled, packageInfo.getAppName()));
            holder.getLineOneTextView().setTextColor(Color.RED);
        } else {
            holder.getLineOneTextView().setText(packageInfo.getAppName());
            holder.getLineOneTextView().setTextColor(mDefaultTextColor);
        }

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

        final CommonPackageInfo commonPackageInfo = getCommonPackageInfos().get(position);
        if (isChoiceMode()) {
            holder.getCheckableImageView().setChecked(commonPackageInfo.isChecked(), false);

            holder.itemView.setOnClickListener(v -> {
                if (isChoiceMode()) {
                    commonPackageInfo.setChecked(!commonPackageInfo.isChecked());
                    holder.getCheckableImageView().setChecked(commonPackageInfo.isChecked());
                    onItemCheckChanged();
                } else {
                    onItemClickNoneChoiceMode(commonPackageInfo, v);
                }
            });
        } else {
            holder.itemView.setOnClickListener(v -> onItemClickNoneChoiceMode(commonPackageInfo, v));
        }

        holder.itemView.setOnLongClickListener(v -> onItemLongClick(v, holder.getAdapterPosition()));
    }

    protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
        Logger.d("onItemClickNoneChoiceMode");
    }

    @Getter
    final BadgeDrawable gcmBadge =
            new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(Color.GRAY)
                    .text1("GCM")
                    .build();

    BadgeDrawable createAppLevelBadge(String levelText, int levelColor) {
        if (levelText == null) return null;
        return new BadgeDrawable.Builder()
                .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                .badgeColor(levelColor)
                .text1(levelText)
                .build();
    }

    protected boolean onBuildGCMIndicator(CommonPackageInfo info, TextView textView) {
        return true;
    }

    void inflatePackageDesc(CommonPackageInfo info, TextView textView, boolean showGcmIndicator) {

        int appLevel = info.getAppLevel();
        String appLevelDesc = getSystemAppIndicatorLabel(appLevel);
        BadgeDrawable appLevelDrawable = createAppLevelBadge(appLevelDesc, getSystemAppIndicatorColor(appLevel));

        List<CharSequence> descSets = new ArrayList<>(2);
        if (appLevelDrawable != null) {
            descSets.add(appLevelDrawable.toSpannable());
        }
        if (onBuildGCMIndicator(info, textView) && showGcmIndicator && info.isGCMSupport()) {
            descSets.add(getGcmBadge().toSpannable());
        }
        if (descSets.size() > 0) {
            CharSequence fullDesc = "";
            for (int i = 0; i < descSets.size(); i++) {
                fullDesc = TextUtils.concat(fullDesc, descSets.get(i), " ");
            }
            SpannableString spannableString =
                    new SpannableString(fullDesc);
            textView.setText(spannableString);
        } else {
            textView.setText(null);
        }
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
        } else if (enableLongPressTriggerAllSelection()) {
            // Select all items.
            selectAll(getCheckedCount() != getItemCount());
            return true;
        }
        return false;
    }

    protected boolean enableLongPressTriggerAllSelection() {
        return true;
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
                info -> {
                    if (info.isChecked()) info.setChecked(false);
                });
    }

    @Override
    public int getItemCount() {
        return commonPackageInfos.size();
    }

    public int getCheckedCount() {
        final int[] c = {0};
        Collections.consumeRemaining(commonPackageInfos,
                info -> {
                    if (info.isChecked()) c[0]++;
                });
        return c[0];
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String appName = getCommonPackageInfos().get(position).getAppName();
        if (appName == null
                || appName.length() < 1) {
            appName = getCommonPackageInfos().get(position).getPkgName();
        }
        if (appName == null) {
            return "*";
        }
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
        @Nullable
        private View moreBtn;

        public CommonViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = itemView.findViewById(android.R.id.title);
            lineTwoTextView = itemView.findViewById(android.R.id.text2);
            systemAppIndicator = itemView.findViewById(android.R.id.text1);
            checkableImageView = itemView.findViewById(R.id.checkable_img_view);
            extraIndicator = itemView.findViewById(R.id.extra_indicator);
            moreBtn = itemView.findViewById(R.id.more);
        }
    }

    public interface ChoiceModeListener {
        void onEnterChoiceMode();

        void onLeaveChoiceMode();
    }
}
