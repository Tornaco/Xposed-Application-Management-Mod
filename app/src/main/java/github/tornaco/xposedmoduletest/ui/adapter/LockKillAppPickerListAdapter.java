package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.view.View;

import github.tornaco.xposedmoduletest.bean.LockKillPackage;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class LockKillAppPickerListAdapter extends LockKillAppListAdapter {

    public LockKillAppPickerListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final LockKillPackage packageInfo = getLockKillPackages().get(position);
        holder.getCheckableImageView().setChecked(!packageInfo.getKill());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageInfo.setKill(!packageInfo.getKill());
                holder.getCheckableImageView().setChecked(!packageInfo.getKill());
            }
        });
    }
}