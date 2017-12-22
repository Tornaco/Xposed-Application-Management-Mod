package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.content.Context;
import android.view.View;

import dev.tornaco.vangogh.display.ImageApplier;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;

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
        return R.layout.app_list_item_2;
    }

    @Override
    protected ImageApplier onCreateImageApplier() {
        return null;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RunningServiceInfoDisplay display = (RunningServiceInfoDisplay) getCommonPackageInfos().get(position);
        holder.getLineTwoTextView().setText(display.getMergedItem().mDescription);
        holder.getSystemAppIndicator().setVisibility(View.VISIBLE);
        holder.getSystemAppIndicator().setText(display.getMergedItem().mSizeStr);
    }

    @Override
    protected boolean onItemLongClick(View v, int position) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
