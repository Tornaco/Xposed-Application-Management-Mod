package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.view.View;

import github.tornaco.xposedmoduletest.bean.PackageInfo;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class GuardAppPickerListAdapter extends GuardAppListAdapter {

    public GuardAppPickerListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final PackageInfo packageInfo = packageInfos.get(position);
        holder.getCheckableImageView().setChecked(packageInfo.getGuard());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageInfo.setGuard(!packageInfo.getGuard());
                holder.getCheckableImageView().setChecked(packageInfo.getGuard());
            }
        });
    }
}