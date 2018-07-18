package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import lombok.Getter;
import tornaco.lib.widget.CheckableImageView;

/**
 * Created by guohao4 on 2017/12/20.
 * Email: Tornaco@163.com
 */

public class RunningServiceAdapter extends CommonPackageInfoAdapter {

    RunningServiceAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getTemplateLayoutRes() {
        return R.layout.app_list_item_running_services;
    }

    @Override
    protected CommonViewHolder onCreateViewHolder(View root) {
        return new RunningServiceInfoHolder(root, super.onCreateViewHolder(root));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RunningServiceInfoHolder runningServiceInfoHolder = (RunningServiceInfoHolder) holder;
        final RunningServiceInfoDisplay display = (RunningServiceInfoDisplay) getCommonPackageInfos().get(position);

        if (display.getMergedItem().mBackground) {
            holder.getLineTwoTextView().setText(R.string.title_cached_background_process);
        } else {
            holder.getLineTwoTextView().setText(display.getMergedItem().mDescription);
        }

        holder.getThirdTextView().setVisibility(View.VISIBLE);
        holder.getThirdTextView().setText(display.getMergedItem().mSizeStr);

        inflatePackageDesc(display, runningServiceInfoHolder.getProcessNameView());
    }

    @Override
    protected void onItemClickNoneChoiceMode(CommonPackageInfo commonPackageInfo, View view) {
        super.onItemClickNoneChoiceMode(commonPackageInfo, view);
        final RunningServiceInfoDisplay display = (RunningServiceInfoDisplay) commonPackageInfo;
        // PerAppSettingsDashboardActivity.start(getContext(), display.getPkgName());
        RunningServicesLoadingCache.getInstance().setMergedItem(display.getMergedItem());
        Bundle args = RunningServiceDetails.makeServiceDetailsActivityBundle(display.getMergedItem());
        RunningServicesDetailsActivity.start(getContext(), args);

        if (display.isSystemApp()) {
            Toast.makeText(getContext(), R.string.system_app_need_ne_careful_running_services,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getSystemAppIndicatorColor(int appLevel) {
        return ContextCompat.getColor(getContext(), R.color.amber_dark);
    }

    @Override
    protected boolean onItemLongClick(View v, int position) {
        return super.onItemLongClick(v, position);
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }

    private static class RunningServiceInfoHolder extends CommonPackageInfoAdapter.CommonViewHolder {
        @Getter
        private CommonViewHolder commonViewHolder;

        @Getter
        private TextView processNameView;

        RunningServiceInfoHolder(View itemView, CommonViewHolder commonViewHolder) {
            super(itemView);
            this.commonViewHolder = commonViewHolder;
            this.processNameView = itemView.findViewById(R.id.text_process);
        }

        @Override
        public TextView getLineOneTextView() {
            return commonViewHolder.getLineOneTextView();
        }

        @Override
        public TextView getLineTwoTextView() {
            return commonViewHolder.getLineTwoTextView();
        }

        @Override
        public TextView getThirdTextView() {
            return commonViewHolder.getThirdTextView();
        }

        @Override
        public View getExtraIndicator() {
            return commonViewHolder.getExtraIndicator();
        }

        @Override
        public CheckableImageView getCheckableImageView() {
            return commonViewHolder.getCheckableImageView();
        }

        @Override
        @Nullable
        public View getMoreBtn() {
            return commonViewHolder.getMoreBtn();
        }
    }
}
