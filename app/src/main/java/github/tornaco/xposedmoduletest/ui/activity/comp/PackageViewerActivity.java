package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.view.View;
import android.widget.TextView;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.loader.PackageLoader;
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

    @Override
    protected void setSummaryView() {
        super.setSummaryView();
        TextView textView = findViewById(R.id.summary);
        textView.setVisibility(View.VISIBLE);
        textView.setText(R.string.summary_comp_edit);
    }

    @Override
    protected List<PackageInfo> performLoading() {
        return PackageLoader.Impl.create(this).loadInstalled(mShowSystemApp);
    }
}
