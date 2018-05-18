package dev.nick.tiles.tile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.R;

public class DropDownTileView extends TileView {

    Spinner mSpinner;
    int mSelectedPosition = -1;

    public DropDownTileView(Context context) {
        super(context);
    }

    public DropDownTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initDropdown() {
        mSpinner = new Spinner(getContext());
        mSpinner.setVisibility(INVISIBLE);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                setSelectedItem(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // noop
            }
        });

    }

    public void setSelectedItem(int position, boolean fromSpinner) {
        if (fromSpinner && position == mSelectedPosition) {
            return;
        }
        mSpinner.setSelection(position);
        mSelectedPosition = mSpinner.getSelectedItemPosition();
        onItemSelected(position);
    }

    protected void onItemSelected(int position) {
        // Noop
    }

    @Override
    protected void onBindActionView(RelativeLayout container) {
        initDropdown();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, onCreateDropDownList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(getInitialSelection());
        int dropDownWidth = getResources().getDimensionPixelSize(R.dimen.drop_down_width);
        container.addView(mSpinner, generateCenterParams(dropDownWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    protected int getInitialSelection() {
        return 0;
    }

    protected List<String> onCreateDropDownList() {
        List<String> list = new ArrayList<String>();
        list.add("Android");
        list.add("Blackberry");
        list.add("Cherry");
        list.add("Duck");
        list.add("Female");
        return list;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        mSpinner.performClick();
    }
}
