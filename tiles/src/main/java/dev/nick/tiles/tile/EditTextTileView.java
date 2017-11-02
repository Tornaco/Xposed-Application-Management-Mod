package dev.nick.tiles.tile;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import dev.nick.tiles.R;

public class EditTextTileView extends TileView {

    EditText mEditText;
    AlertDialog mAlertDialog;

    public EditTextTileView(Context context) {
        super(context);
    }

    public EditTextTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
        View editTextContainer = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null, false);
        mEditText = (EditText) editTextContainer.findViewById(R.id.edit_text);
        mEditText.setHint(getHint());
        mEditText.setInputType(getInputType());
        mAlertDialog = new AlertDialog.Builder(context)
                .setView(editTextContainer)
                .setTitle(getDialogTitle())
                .setPositiveButton(getPositiveButton(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveButtonClick();
                    }
                })
                .setNegativeButton(getNegativeButton(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onNegativeButtonClick();
                    }
                })
                .create();
    }

    protected int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }

    protected CharSequence getHint() {
        return null;
    }

    protected EditText getEditText() {
        return mEditText;
    }

    protected CharSequence getDialogTitle() {
        return "Edit tile";
    }

    protected CharSequence getPositiveButton() {
        return "SAVE";
    }

    protected CharSequence getNegativeButton() {
        return "DISCARD";
    }

    protected void onPositiveButtonClick() {
        // Nothing.
    }

    protected void onNegativeButtonClick() {
        // Nothing.
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        mAlertDialog.show();
    }
}
