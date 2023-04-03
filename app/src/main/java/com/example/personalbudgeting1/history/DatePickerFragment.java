package com.example.personalbudgeting1.history;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.N)
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private final Consumer<String> consumer;

    public DatePickerFragment(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        int months = month + 1;
        String day = "", mon = "";
        if (dayOfMonth < 10) {
            day += "0" + dayOfMonth;
        } else {
            day = "" + dayOfMonth;
        }
        if (months < 10) {
            mon += "0" + months;
        } else {
            mon = "" + months;
        }
        String date = day + "-" + mon + "-" + year;
        consumer.accept(date);
    }
}

