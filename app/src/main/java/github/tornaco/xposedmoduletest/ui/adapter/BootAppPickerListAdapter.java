package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.view.View;

import github.tornaco.xposedmoduletest.bean.BootCompletePackage;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class BootAppPickerListAdapter extends BootAppListAdapter {

    public BootAppPickerListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final BootCompletePackage packageInfo = getBootCompletePackages().get(position);
        holder.getCheckableImageView().setChecked(packageInfo.getAllow());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageInfo.setAllow(!packageInfo.getAllow());
                holder.getCheckableImageView().setChecked(packageInfo.getAllow());
            }
        });
    }
}