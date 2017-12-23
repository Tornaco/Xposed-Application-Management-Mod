package github.tornaco.xposedmoduletest.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.AccessInfo;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.util.MediaUtil;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.AccessiewHolder> {

    private Context context;

    public PhotoListAdapter(Context context) {
        this.context = context;
    }

    private final List<AccessInfo> accessInfos = new ArrayList<>();

    public void update(Collection<AccessInfo> src) {
        synchronized (accessInfos) {
            accessInfos.clear();
            accessInfos.addAll(src);
        }
        notifyDataSetChanged();
    }

    @Override
    public AccessiewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(getTemplateLayoutRes(), parent, false);
        return new AccessiewHolder(view);
    }

    @LayoutRes
    private int getTemplateLayoutRes() {
        return R.layout.photo_list_item;
    }

    @Override
    public void onBindViewHolder(final AccessiewHolder holder, int position) {
        final AccessInfo accessInfo = accessInfos.get(position);
        holder.textView.setText(FuzzyDateTimeFormatter.getTimeAgo(context, new Date(accessInfo.getWhen())));
        holder.textView2.setText(accessInfo.getUrl());

        GlideApp.with(context)
                .load(accessInfo.getUrl())
                .placeholder(0)
                .error(0)
                .fallback(0)
                .transition(withCrossFade())
                .into(holder.imageView);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public boolean onLongClick(View v) {
                DaoManager daoManager = DaoManager.getInstance();
                DaoSession daoSession = daoManager.getSession(context);
                if (daoSession == null) return false;
                daoSession.getAccessInfoDao().delete(accessInfo);
                File del = new File(accessInfo.getUrl());
                del.delete();
                onAccessInfoDetele(accessInfo);
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(MediaUtil.buildOpenIntent(context, new File(accessInfo.getUrl())));
            }
        });
    }

    protected void onAccessInfoDetele(AccessInfo accessInfo) {

    }

    @Override
    public int getItemCount() {
        return accessInfos.size();
    }

    static class AccessiewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView, textView2;

        AccessiewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo);
            textView = itemView.findViewById(android.R.id.text1);
            textView2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
