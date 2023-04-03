package com.example.personalbudgeting1.history;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.personalbudgeting1.R;

import java.util.function.Consumer;


public class HistorySearchFragment extends Fragment {

    private View parentView;
    private String startDate;
    private String endDate;

    public HistorySearchFragment() {
        super(R.layout.fragment_history_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundleInstance) {
        super.onViewCreated(view, savedBundleInstance);
        this.parentView = view;

        Button startDateButton = parentView.findViewById(R.id.startDate);
        Button endDateButton = parentView.findViewById(R.id.endDate);
        Button searchActivityHistory = parentView.findViewById(R.id.searchActivityHistory);

        startDateButton.setOnClickListener(v -> showDatePickerDialog(this::setStartDate));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(this::setEndDate));

        searchActivityHistory.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("startDate", startDate);
            result.putString("endDate", endDate);
            getParentFragmentManager().setFragmentResult("requestKey", result);
        });
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        TextView chosenStartDate = parentView.findViewById(R.id.chosenStartDate);
        chosenStartDate.setText(startDate);
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
        TextView chosenEndDate = parentView.findViewById(R.id.chosenEndDate);
        chosenEndDate.setText(endDate);
    }

    private void showDatePickerDialog(Consumer<String> consumer) {
        DialogFragment newFragment = new DatePickerFragment(consumer);
        newFragment.show(getParentFragmentManager(), "datePicker");
    }
}
