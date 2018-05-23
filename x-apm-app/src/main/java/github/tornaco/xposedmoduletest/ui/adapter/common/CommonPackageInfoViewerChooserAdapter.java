package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoViewerChooserAdapter extends CommonPackageInfoAdapter {

    @Getter
    @Setter
    private AdapterView.OnItemClickListener onItemClickListener;

    public CommonPackageInfoViewerChooserAdapter(Context context) {
        super(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final CommonPackageInfo packageInfo = commonPackageInfos.get(position);

        holder.getCheckableImageView().setChecked(packageInfo.isChecked(), false);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChoiceMode()) {
                    packageInfo.setChecked(!packageInfo.isChecked());
                    holder.getCheckableImageView().setChecked(packageInfo.isChecked());
                } else {
                    onItemClickListener.onItemClick(null, v, holder.getAdapterPosition(), holder.getItemId());
                }
            }
        });
    }

}
