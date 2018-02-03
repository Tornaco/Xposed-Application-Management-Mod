package github.tornaco.xposedmoduletest.xposed.service.ops;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/2/2.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
@ToString
public class PermissionDialog implements View.OnClickListener {

    private OpsArgs args;

    public void display(Context context) {
        onCreateVerifierDialog(context)
                .show();
    }

    private View onCreateAndBindVerifierView(Context context) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(false);
        checkBox.setOnClickListener(this);
        return checkBox;
    }

    @SuppressWarnings("ConstantConditions")
    private Dialog onCreateVerifierDialog(Context context) {
        Dialog d = new AlertDialog.Builder(context)
                .setTitle("XXXX")
                .setMessage("XXXX")
                .setCancelable(false)
                .setView(onCreateAndBindVerifierView(context))
                .setPositiveButton(android.R.string.yes, null)
                .setNegativeButton(android.R.string.no, null)
                .create();
        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        return d;
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        args.setRemember(checkBox.isChecked());
    }
}
