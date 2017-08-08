package com.applozic.mobicomkit.uiwidgets.schedule;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private View scheduledDateView;
    private ScheduledTimeHolder scheduledTimeHolder;
    private String[] monthNames = {"Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private TextView selectedDate;

    public SelectDateFragment() {

    }

    public void setScheduledDateView(View scheduledDateView) {
        this.scheduledDateView = scheduledDateView;
    }

    public void setScheduledTimeHolder(ScheduledTimeHolder scheduledTimeHolder) {
        this.scheduledTimeHolder = scheduledTimeHolder;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        if (scheduledTimeHolder.getTimestamp() != null) {
            String scheduledDate = scheduledTimeHolder.getDate();
            String[] sd = scheduledDate.split("-");
            datePickerDialog.updateDate(Integer.parseInt(sd[2]), Integer.parseInt(sd[1]), Integer.parseInt(sd[0]));
        }
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        scheduledTimeHolder.setDate(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
        selectedDate.setText(dayOfMonth + "-" + monthNames[monthOfYear] + "-" + year);
    }
}