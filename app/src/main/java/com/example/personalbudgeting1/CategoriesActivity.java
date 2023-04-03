package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CategoriesActivity extends AppCompatActivity {
    private Utils utils;
    private TextView totalBudgetAmountTv;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private Spinner itemSpinner;
    private TodayItemsAdapter adapter;
    private List<Data> myDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        utils = Utils.getInstance();
        itemSpinner = findViewById(R.id.itemsspinner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        totalBudgetAmountTv = findViewById(R.id.totalBudgetAmountTv);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Categories");
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        itemSpinner.setAdapter(spinnerArrayAdapter);
        try {
            loadCategory();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        myDataList = new ArrayList<>();
        adapter = new TodayItemsAdapter(CategoriesActivity.this,myDataList);
        recyclerView.setAdapter(adapter);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!itemSpinner.getSelectedItem().equals("Select item")) {
                    Query query = FirebaseDatabase.getInstance().getReference("expenses").child(Objects.requireNonNull(utils.getAuth().getUid())).orderByChild("item").equalTo(itemSpinner.getSelectedItem().toString());
                    query.addValueEventListener(new ValueEventListener() {
                        @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            myDataList.clear();
                            float total = 0f;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Data data = ds.getValue(Data.class);
                                myDataList.add(data);
                                assert data != null;
                                total += data.getAmount();
                            }
                            adapter.notifyDataSetChanged();
                            totalBudgetAmountTv.setText("You spent $" + String.format("%.2f", total) + " for " + itemSpinner.getSelectedItem().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void loadCategory() throws ExecutionException, InterruptedException {
        categories.clear();
        categories.add("Select item");
        categories.add("Transport");
        categories.add("Food");
        categories.add("House");
        categories.add("Entertainment");
        categories.add("Education");
        categories.add("Charity");
        categories.add("Apparel");
        categories.add("Health");
        categories.add("Personal");
        categories.add("Other");
        Query query = FirebaseDatabase.getInstance().getReference("categories").child(Objects.requireNonNull(utils.getAuth().getUid()));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesNew.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Object obj = ds.getValue();
                    assert obj != null;
                    categoriesNew.add(obj.toString());
                }
                categories.addAll(categoriesNew);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        spinnerArrayAdapter.notifyDataSetChanged();
    }
}