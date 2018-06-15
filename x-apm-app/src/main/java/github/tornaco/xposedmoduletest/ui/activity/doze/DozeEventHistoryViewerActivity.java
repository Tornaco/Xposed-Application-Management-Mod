package github.tornaco.xposedmoduletest.ui.activity.doze;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListPickerActivity;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.common.CommonPackageInfoViewerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;

/**
 * Created by guohao4 on 2018/1/5.
 * Email: Tornaco@163.com
 */

public class DozeEventHistoryViewerActivity extends CommonPackageInfoListPickerActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, DozeEventHistoryViewerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void initView() {
        super.initView();
        fab.hide();
    }

    @Override
    protected void doOnFabClickInWorkThread() {

    }

    @Override
    protected CommonPackageInfoAdapter onCreateAdapter() {

        return new CommonPackageInfoViewerAdapter(this) {
            @Override
            protected int getTemplateLayoutRes() {
                return R.layout.app_list_item_2_more_line;
            }

            @Override
            protected boolean imageLoadingEnabled() {
                return false;
            }

            @Override
            public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                final CommonPackageInfo packageInfo = getCommonPackageInfos().get(position);
                String startTime = packageInfo.getPayload()[0];
                String lastState = packageInfo.getPayload()[2];
                String fail = packageInfo.getPayload()[3];

                holder.getThirdTextView().setText(startTime);
                holder.getThirdTextView().setVisibility(View.VISIBLE);

                if (!packageInfo.isChecked()) {
                    holder.getLineTwoTextView().setText(getString(R.string.title_doze_history_summary,
                            fail, lastState));
                    holder.getLineTwoTextView().setVisibility(View.VISIBLE);
                } else {
                    holder.getLineTwoTextView().setVisibility(View.GONE);
                }

                holder.getCheckableImageView().setVisibility(View.INVISIBLE);
                holder.getCheckableImageView().setChecked(false, false);
            }
        };
    }

    @Override
    protected List<? extends CommonPackageInfo> performLoading() {
        List<DozeEvent> dozeEvents = XAPMManager.get().getDozeEventHistory();
        List<CommonPackageInfo> res = new ArrayList<>(dozeEvents.size());
        for (DozeEvent e : dozeEvents) {

            // FIXME Logging this wtf.
            if (e == null) continue;

            if (e.getStartTimeMills() <= 0) continue;

            CommonPackageInfo c = new CommonPackageInfo();
            c.setAppName(getString(resultToStringRes(e.getResult())));
            c.setPkgName(getPackageName());
            c.setSystemApp(false);
            c.setDisabled(false);
            c.setChecked(e.getResult() == DozeEvent.RESULT_SUCCESS);

            String startTimeMills = FuzzyDateTimeFormatter.getTimeAgo(getContext(),
                    new Date(e.getStartTimeMills()));

            String endTimeMills = FuzzyDateTimeFormatter.getTimeAgo(getContext(),
                    new Date(e.getEndTimeMills()));

            String failCodeStr = getString(failCodeToStringRes(e.getFailCode()));

            int lastState = e.getLastState();
            String lastStateStr = DeviceIdleControllerProxy.stateToString(lastState);

            String[] payload = new String[]{startTimeMills, endTimeMills, lastStateStr, failCodeStr};
            c.setPayload(payload);

            res.add(c);
        }
        return res;
    }

    @StringRes
    private int failCodeToStringRes(int code) {
        switch (code) {
            case DozeEvent.FAIL_DEVICE_INTERACTIVE:
                return R.string.title_doze_fail_device_interactive;
            case DozeEvent.FAIL_POWER_CHARGING:
                return R.string.title_doze_fail_device_charging;
            case DozeEvent.FAIL_RETRY_TIMEOUT:
                return R.string.title_doze_fail_device_max;
            case DozeEvent.FAIL_GENERIC_FAILURE:
                return R.string.title_doze_fail_generic_failure;
            case DozeEvent.FAIL_UNKNOWN:
                return R.string.title_doze_res_unknown;
        }
        return R.string.title_doze_res_unknown;
    }

    @StringRes
    private int resultToStringRes(int res) {
        switch (res) {
            case DozeEvent.RESULT_FAIL:
                return R.string.title_doze_res_fail;
            case DozeEvent.RESULT_SUCCESS:
                return R.string.title_doze_res_success;
            case DozeEvent.RESULT_UNKNOWN:
                return R.string.title_doze_res_unknown;
            case DozeEvent.RESULT_PENDING:
                return R.string.title_doze_res_pending;
        }
        return R.string.title_doze_res_unknown;
    }
}
