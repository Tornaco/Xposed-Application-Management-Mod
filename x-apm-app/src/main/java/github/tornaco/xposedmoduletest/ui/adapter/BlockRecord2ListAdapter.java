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

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class BlockRecord2ListAdapter extends RecyclerView.Adapter<BlockRecord2ListAdapter.AppViewHolder>
        implements SectionIndexer {

    private Context context;

    public BlockRecord2ListAdapter(Context context) {
        this.context = context;
    }

    private final List<BlockRecord2> blockRecords = new ArrayList<>();

    public void update(Collection<BlockRecord2> src) {
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

    public List<BlockRecord2> getBlockRecords() {
        return blockRecords;
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.block_record_list_item;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final BlockRecord2 blockRecord = blockRecords.get(position);
        holder.getLineOneTextView().setText(PkgUtil.loadNameByPkgName(context, blockRecord.getPkgName()));

        holder.getLineTwoTextView().setText(
                context.getString(R.string.block_record_summary,
                        String.valueOf(blockRecord.getHowManyTimesBlocked()),
                        String.valueOf(blockRecord.getHowManyTimesAllowed()),
                        FuzzyDateTimeFormatter.getTimeAgo(context, new Date(blockRecord.getTimeWhen())),
                        BlockRecord2.decodeType(blockRecord.getType()),
                        blockRecord.isBlock() ? "阻止" : "允许",
                        blockRecord.getReason(),
                        PkgUtil.loadNameByPkgName(context, blockRecord.getCallerPkgName())));

        onBindItemImageView(blockRecord, holder.getAppIconView());

        holder.itemView.setOnClickListener(v -> onListItemClick(blockRecord));
    }

    protected void onListItemClick(BlockRecord2 blockRecord) {
        // Noop.
    }

    protected void onBindItemImageView(BlockRecord2 blockRecord, ImageView imageView) {
        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(blockRecord.getPkgName());
        GlideApp.with(context)
                .load(c)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .transition(withCrossFade())
                .into(imageView);
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
