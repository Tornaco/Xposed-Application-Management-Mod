package dev.nick.tiles.tile;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeTileView extends TileView {

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private OnDateSetListener listener;
    private long time;

    public TimeTileView(Context context, long time) {
        super(context);
        this.time = time;
    }

    public TimeTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        mDatePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mTimePickerDialog.show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        mTimePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                listener.onDateSet(mDatePickerDialog.getDatePicker(),
                        mDatePickerDialog.getDatePicker().getYear(),
                        mDatePickerDialog.getDatePicker().getMonth(),
                        mDatePickerDialog.getDatePicker().getDayOfMonth(),
                        hourOfDay, minute);
            }
        }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        mDatePickerDialog.show();
    }

    public interface OnDateSetListener {
        /**
         * @param view       the picker associated with the dialog
         * @param year       the selected year
         * @param month      the selected month (0-11 for compatibility with
         *                   {@link Calendar#MONTH})
         * @param dayOfMonth th selected day of the month (1-31, depending on
         *                   month)
         */
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth, int hourOfDay, int minute);
    }
}
