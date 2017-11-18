package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.view.View;

import github.tornaco.xposedmoduletest.ui.activity.ag.GuardAppPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.GuardAppListAdapter;

/**
 * Created by guohao4 on 2017/11/18.
 * Email: Tornaco@163.com
 */

public class PackageViewerActivity extends GuardAppPickerActivity {

    @Override
    protected GuardAppListAdapter onCreateAdapter() {
        return new GuardAppListAdapter(this) {
            @Override
            public void onBindViewHolder(final AppViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                holder.itemView.setOnLongClickListener(null);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ComponentEditorActivity.start(getActivity(),
                                getPackageInfos()
                                        .get(holder.getAdapterPosition())
                                        .getPkgName());
                    }
                });
            }
        };
    }
}
