package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoPickerAdapter extends CommonPackageInfoAdapter {

    public CommonPackageInfoPickerAdapter(Context context) {
        super(context);
        setChoiceMode(true);
    }

    @LayoutRes
    protected int getTemplateLayoutRes() {
        return R.layout.app_list_item_2;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final CommonPackageInfo packageInfo = commonPackageInfos.get(position);
        holder.getLineTwoTextView().setText(packageInfo.getPkgName());
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
