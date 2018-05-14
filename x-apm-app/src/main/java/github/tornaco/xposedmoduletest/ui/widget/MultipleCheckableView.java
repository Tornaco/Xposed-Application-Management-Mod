package github.tornaco.xposedmoduletest.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/25.
 * Email: Tornaco@163.com
 */
@Getter
public class MultipleCheckableView extends RecyclerView {

    private List<CheckableController> data;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class CheckableController {
        private int titleRes;
        private boolean isChecked;
        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    }

    public MultipleCheckableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Context context) {
        setHasFixedSize(true);
        setLayoutManager(createLayoutManager(context, data.size()));
        setAdapter(new InternalAdapter());
    }

    public void setData(List<CheckableController> data) {
        this.data = data;
        init(getContext());
    }

    private static LayoutManager createLayoutManager(Context context, int childCount) {
        LayoutManager manager;
        switch (childCount) {
            case 1:
                manager = new GridLayoutManager(context, 1);
                break;
            case 2:
                manager = new GridLayoutManager(context, 2);
                break;
            default:
                manager = new GridLayoutManager(context, 3);
                break;
        }
        return manager;
    }

    private class InternalAdapter extends RecyclerView.Adapter<InternalHolder> {

        @Override
        public InternalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new InternalHolder(LayoutInflater.from(getContext())
                    .inflate(R.layout.simple_checkbox_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final InternalHolder holder, final int position) {
            CheckableController checkableController = getData().get(position);
            holder.getTitleView().setText(checkableController.getTitleRes());
            holder.getCheckBox().setChecked(checkableController.isChecked());
            holder.getCheckBox().setOnCheckedChangeListener(checkableController.getOnCheckedChangeListener());
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }
    }


    @Getter
    static final class InternalHolder extends ViewHolder {

        private CheckBox checkBox;
        private TextView titleView;

        public InternalHolder(View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.checkbox);
            this.titleView = itemView.findViewById(R.id.item_title);
        }
    }
}
