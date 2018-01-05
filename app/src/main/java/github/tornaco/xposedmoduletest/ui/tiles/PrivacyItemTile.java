package github.tornaco.xposedmoduletest.ui.tiles;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import dev.nick.tiles.tile.QuickTile;
import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2018/1/5.
 * Email: Tornaco@163.com
 */

public class PrivacyItemTile extends QuickTile {

    public PrivacyItemTile(Context context) {
        super(context);
    }

    void showEditTextDialog(Activity activity, final EditTextAction action) {
        final EditText e = new EditText(activity);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.title_privacy_input_code)
                .setView(e)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = e.getText().toString();
                        action.onAction(text);
                    }
                })
                .show();
    }

    interface EditTextAction {
        void onAction(String text);
    }
}
