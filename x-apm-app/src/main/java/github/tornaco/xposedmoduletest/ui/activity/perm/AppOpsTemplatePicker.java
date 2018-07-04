package github.tornaco.xposedmoduletest.ui.activity.perm;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;

/**
 * Created by Tornaco on 2018/7/4 11:32.
 * This file is writen for project X-APM at host guohao4.
 */
public class AppOpsTemplatePicker {

    public interface SinglePickerListener {
        void onAppOpsTemplatePick(@Nullable AppOpsTemplate template);
    }

    @UiThread
    public static void chooseOne(@NonNull Activity activity,
                                 @Nullable AppOpsTemplate originTemplate,
                                 @NonNull SinglePickerListener pickerListener) {
        List<AppOpsTemplate> appOpsTemplateList = XAPMManager.get()
                .getAppOpsTemplates();

        String[] alias;
        boolean hasTemplate = appOpsTemplateList != null && appOpsTemplateList.size() > 0;

        int selection = -1; /* No item checked */

        if (!hasTemplate) {
            alias = new String[]{activity.getString(R.string.app_ops_template_choice_none)};
            selection = 0; /* Default */
        } else {
            alias = new String[appOpsTemplateList.size()];
            for (int i = 0; i < appOpsTemplateList.size(); i++) {
                alias[i] = appOpsTemplateList.get(i).getAlias();
                if (originTemplate != null
                        && originTemplate.getId() != null
                        && originTemplate.getId().equals(appOpsTemplateList.get(i).getId())) {
                    selection = i;
                }
            }
        }

        AtomicReference<AppOpsTemplate> selectedTemplate = new AtomicReference<>(null);

        new AlertDialog.Builder(activity).setCancelable(false)
                .setTitle(R.string.title_app_ops_template_picker)
                .setSingleChoiceItems(alias, selection, (dialog, which) -> {
                    if (hasTemplate) {
                        selectedTemplate.set(appOpsTemplateList.get(which));
                    } else {
                        selectedTemplate.set(null);
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> pickerListener.onAppOpsTemplatePick(selectedTemplate.get()))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> pickerListener.onAppOpsTemplatePick(null))
                .setNeutralButton(R.string.app_ops_template_create_new, (dialog, which) -> AppOpsTemplateEditorActivity.start(activity, null))
                .show();
    }
}
