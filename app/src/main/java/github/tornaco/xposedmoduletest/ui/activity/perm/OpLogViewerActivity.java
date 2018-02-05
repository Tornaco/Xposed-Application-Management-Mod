package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.loader.LoaderUtil;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoViewerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by guohao4 on 2018/1/31.
 * Email: Tornaco@163.com
 */

public class OpLogViewerActivity extends CommonPackageInfoListActivity {

    public static void start(Context context, String pkg, int op) {
        Logger.w("OpLogViewerActivity start: " + pkg + "-" + op);
        Intent starter = new Intent(context, OpLogViewerActivity.class);
        starter.putExtra("tor_pkg", pkg);
        starter.putExtra("tor_op", op);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    private String mPackageName;
    private int mOp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPackageName = getIntent().getStringExtra("tor_pkg");
        mOp = getIntent().getIntExtra("tor_op", -1);

        if (mPackageName != null) {
            setTitle(PkgUtil.loadNameByPkgName(getContext(), mPackageName));
        } else if (mOp >= 0) {
            setTitle(AppOpsManagerCompat.getOpLabel(getContext(), mOp));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_clear_all_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear.
                if (mPackageName != null) {
                    XAshmanManager.get().clearOpLogForPackage(mPackageName);
                } else if (mOp >= 0) {
                    XAshmanManager.get().clearOpLogForOp(mOp);
                }
                startLoading();
            }
        });
    }


    @Override
    protected int getSummaryRes() {
        return 0;
    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {
        return new CommonPackageInfoViewerAdapter(this) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                OpLog log = (OpLog) packageInfo.getArgs();
                String timeStr = FuzzyDateTimeFormatter.getTimeAgo(getContext(),
                        new Date(log.getWhen()));
                int mode = log.getMode();
                String modeStr = mode == AppOpsManagerCompat.MODE_ALLOWED ? getString(R.string.mode_allowed)
                        : getString(R.string.mode_ignored);
                holder.getLineTwoTextView().setText(timeStr + "\t" + modeStr + "\t"
                        + Arrays.toString(log.getPayload()));
                // This is query by package.
                if (mPackageName != null) {
                    String title = AppOpsManagerCompat.getOpLabel(getContext(), log.getCode());
                    holder.getLineOneTextView().setText(title);
                    holder.getCheckableImageView().setImageDrawable(ContextCompat
                            .getDrawable(getContext(), AppOpsManagerCompat.opToIconRes(log.getCode())));
                }
            }

            @Override
            protected boolean imageLoadingEnabled() {
                return mPackageName == null;
            }

            @Override
            protected int getTemplateLayoutRes() {
                if (mPackageName != null) {
                    return R.layout.app_list_item_2_op_log;
                }
                return R.layout.app_list_item_2_op_log_pkg;
            }
        };
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        if (!TextUtils.isEmpty(mPackageName)) {
            List<OpLog> oplogs = XAshmanManager.get().getOpLogForPackage(mPackageName);
            if (oplogs != null) {
                List<CommonPackageInfo> ps = new ArrayList<>(oplogs.size());
                for (OpLog l : oplogs) {
                    if (l.getCode() >= AppOpsManagerCompat._NUM_OP) {
                        continue;
                    }
                    CommonPackageInfo info = new CommonPackageInfo();
                    info.setArgs(l);
                    ps.add(info);
                }
                Collections.reverse(ps);
                return ps;
            }
        } else if (mOp >= 0) {
            List<OpLog> oplogs = XAshmanManager.get().getOpLogForOp(mOp);
            if (oplogs != null) {
                List<CommonPackageInfo> ps = new ArrayList<>(oplogs.size());
                for (OpLog l : oplogs) {
                    if (l.getCode() >= AppOpsManagerCompat._NUM_OP) {
                        continue;
                    }
                    CommonPackageInfo info = LoaderUtil.constructCommonPackageInfo(getContext(), l.getPackageName());
                    if (info == null) continue;
                    info.setArgs(l);
                    ps.add(info);
                }
                Collections.reverse(ps);
                return ps;
            }
        }
        return new ArrayList<>(0);
    }
}
