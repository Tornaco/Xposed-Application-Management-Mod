package github.tornaco.xposedmoduletest.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.widget.RecyclerView;

import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/25.
 * Email: Tornaco@163.com
 */

public class MultipleCheckableAdapter {

    @Getter
    public final class MultipleCheckBoxViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView checkboxContainer;
        private ImageView iconView;
        private TextView titleView;

        public MultipleCheckBoxViewHolder(View itemView) {
            super(itemView);
        }
    }
}
