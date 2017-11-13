package github.tornaco.xposedmoduletest.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.FadeOutFadeInApplier;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.BlockRecord;
import github.tornaco.xposedmoduletest.loader.VangoghAppLoader;
import github.tornaco.xposedmoduletest.ui.activity.BlockRecordViewerActivity;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class BlockRecordListAdapter extends RecyclerView.Adapter<BlockRecordListAdapter.AppViewHolder>
        implements SectionIndexer {

    private Context context;
    private VangoghAppLoader vangoghAppLoader;

    public BlockRecordListAdapter(Context context) {
        this.context = context;
        vangoghAppLoader = new VangoghAppLoader(context);
    }

    final List<BlockRecord> blockRecords = new ArrayList<>();

    public void update(Collection<BlockRecord> src) {
        synchronized (blockRecords) {
            blockRecords.clear();
            blockRecords.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AppViewHolder(view);
    }

    public List<BlockRecord> getBlockRecords() {
        return blockRecords;
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.block_record_list_item;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final BlockRecord blockRecord = blockRecords.get(position);
        holder.getLineOneTextView().setText(blockRecord.getAppName());
        holder.getLineTwoTextView().setText(blockRecord.getDescription()
                + "\n" + (blockRecord.getAllow() ? "ALLOWED" : "BLOCKED")
                + "\n" + blockRecord.getWhy()
                + "\n" + FuzzyDateTimeFormatter.getTimeAgo(context, new Date(blockRecord.getTimeWhen())));
        Vangogh.with(context)
                .load(blockRecord.getPkgName())
                .skipMemoryCache(true)
                .usingLoader(vangoghAppLoader)
                .applier(new FadeOutFadeInApplier())
                .placeHolder(0)
                .fallback(R.mipmap.ic_launcher_round)
                .into(holder.getAppIconView());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlockRecordViewerActivity.start(context, blockRecord.getPkgName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return blockRecords.size();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        private TextView lineOneTextView;
        private TextView lineTwoTextView;
        private ImageView appIconView;

        AppViewHolder(View itemView) {
            super(itemView);
            lineOneTextView = (TextView) itemView.findViewById(android.R.id.title);
            lineTwoTextView = (TextView) itemView.findViewById(android.R.id.text1);
            appIconView = (ImageView) itemView.findViewById(R.id.icon);
        }

        TextView getLineOneTextView() {
            return lineOneTextView;
        }

        TextView getLineTwoTextView() {
            return lineTwoTextView;
        }

        ImageView getAppIconView() {
            return appIconView;
        }
    }
}
