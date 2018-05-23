package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.AdapterView;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoViewerAdapter extends CommonPackageInfoAdapter {

    @Getter
    @Setter
    private AdapterView.OnItemClickListener onItemClickListener;

    public CommonPackageInfoViewerAdapter(Context context) {
        super(context);
        setChoiceMode(true);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommonViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, v, holder.getAdapterPosition(), holder.getItemId());
            }
        });
    }


}
