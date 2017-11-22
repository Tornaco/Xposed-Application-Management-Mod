package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.EmojiUtil;

public class DonateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);

        setupToolbar();
        showHomeAsUp();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView textView = findViewById(R.id.donate_text);
        String fivemore = duplicateString(EmojiUtil.getEmojiByUnicode(EmojiUtil.FIVE_MORE), 16);
        textView.setText(getString(R.string.donate_intro, fivemore, fivemore,
                duplicateString(EmojiUtil.getEmojiByUnicode(EmojiUtil.DOG), 3)));

        // For DEBUG.
        Logger.e("WUMAO: " + EmojiUtil.getEmojiByUnicode(EmojiUtil.FIVE_MORE));
    }


    private String duplicateString(String s, int count) {
        StringBuilder out = new StringBuilder(s);
        for (int i = 0; i < count; i++) {
            out.append(s);
        }
        return out.toString();
    }
}
