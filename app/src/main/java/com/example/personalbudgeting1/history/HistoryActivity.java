package com.example.personalbudgeting1.history;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalbudgeting1.Data;
import com.example.personalbudgeting1.R;
import com.example.personalbudgeting1.TodayItemsAdapter;
import com.example.personalbudgeting1.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoryActivity extends AppCompatActivity {
    private TextView historyTotalAmountSpent;
    private RecyclerView recycler_View_Id_Feed;
    private Utils utils;
    private TodayItemsAdapter todayItemsAdapter;
    private LinearLayout historyLayout;
    private List<Data> myDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        utils = Utils.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        historyLayout = findViewById(R.id.historyLayout);
        historyTotalAmountSpent = findViewById(R.id.historyTotalAmountSpent);
        recycler_View_Id_Feed = findViewById(R.id.recycler_View_Id_Feed);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("History");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler_View_Id_Feed.setLayoutManager(layoutManager);
        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(HistoryActivity.this, myDataList);
        recycler_View_Id_Feed.setAdapter(todayItemsAdapter);

        if (savedInstanceState == null) {
            getSupportFragmentManager().setFragmentResultListener("requestKey", this, (FragmentResultListener) (requestKey, bundle) -> {
                String startDate = bundle.getString("startDate");
                String endDate = bundle.getString("endDate");
                queryActivity(startDate, endDate);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void queryActivity(String startDate, String endDate) {
        Query query = utils.getExpensesRef().orderByChild("date");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Data data = snapshot.getValue(Data.class);

                    myDataList.add(data);
                }
                myDataList.removeIf(data -> hasDateOutsideInterval(data, endDate, startDate));
                todayItemsAdapter.notifyDataSetChanged();
                recycler_View_Id_Feed.setVisibility(View.VISIBLE);

                float totalAmount = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total = data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount += pTotal;
                    if (totalAmount > 0) {
                        historyLayout.setVisibility(View.VISIBLE);
                        historyTotalAmountSpent.setText("This day you spent $ " + String.format("%.2f", totalAmount));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasDateOutsideInterval(Data data, String endDate, String startDate) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(data.getDate(), dtf);
        return date.isAfter(LocalDate.parse(endDate, dtf)) || date.isBefore(LocalDate.parse(startDate, dtf));
    }
}