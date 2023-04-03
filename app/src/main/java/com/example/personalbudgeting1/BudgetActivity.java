package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;

public class BudgetActivity extends AppCompatActivity {
    private Utils utils;
    private TextView totalBudgetAmountTv;
    private RecyclerView recyclerView;
    private String post_key = "";
    private String item = "";
    private double amount = 0.0;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private final UserEmail userEmail = UserEmail.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        totalBudgetAmountTv = findViewById(R.id.totalBudgetAmountTv);
        recyclerView = findViewById(R.id.recyclerView);
        utils=Utils.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        if(!getIntent().getExtras().get("email").equals("email")){
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(view -> additem());
        loadCategory();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        utils.getBudgetRef().addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalAmount = 0.0;

                for(DataSnapshot snap:snapshot.getChildren()){
                    Data data = snap.getValue(Data.class);
                    assert data != null;
                    double total = data.getAmount();
                    double pTotal = Double.parseDouble(String.valueOf(total));
                    totalAmount+= pTotal;
                }
                totalBudgetAmountTv.setText("Total budget amount $"+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Month Budget");
    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout,null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        final Spinner itemSpinner = myView.findViewById(R.id.itemsspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);
        final CheckBox monthlyCheckbox = myView.findViewById(R.id.monthlyCheckbox);
        final EditText numberMonth = myView.findViewById(R.id.numberMonth);
        spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        itemSpinner.setAdapter(spinnerArrayAdapter);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(itemSpinner.getSelectedItem().toString().equals("New category")){
                    addCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        monthlyCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(monthlyCheckbox.isChecked()){
                numberMonth.setVisibility(View.VISIBLE);
            }else{
                numberMonth.setVisibility(View.GONE);
            }
        });
        save.setOnClickListener(view -> {
            String budgetAmount = amount.getText().toString();
            String budgetItem = itemSpinner.getSelectedItem().toString();
            if(TextUtils.isEmpty(budgetAmount)){
                amount.setError("Amount is required!");
                return;
            }

            if(budgetItem.equals("Select item")){
                Toast.makeText(BudgetActivity.this,"Select a valid item",Toast.LENGTH_SHORT).show();

            }else if(monthlyCheckbox.isChecked()){
                utils.saveBudget(BudgetActivity.this, budgetItem,"", Double.parseDouble(budgetAmount),monthlyCheckbox.isChecked(),Integer.parseInt(numberMonth.getText().toString()),0, Double.parseDouble(budgetAmount),"");
                dialog.dismiss();
                utils.progressDialog.dismiss();
            }else{
                utils.saveBudget(BudgetActivity.this, budgetItem,"", Float.parseFloat(budgetAmount),monthlyCheckbox.isChecked(),0,0,0,"");
                dialog.dismiss();
                utils.progressDialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
    private void addCategory() {
        EditText categoryName;
        Button addCategory;
        androidx.appcompat.app.AlertDialog.Builder myDialog = new androidx.appcompat.app.AlertDialog.Builder(this,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.addcategory_layout,null);
        myDialog.setView(myView);
        final androidx.appcompat.app.AlertDialog dialog = myDialog.create();
        categoryName = myView.findViewById(R.id.categoryName);
        addCategory = myView.findViewById(R.id.addCategory);
        dialog.show();
        addCategory.setOnClickListener(view -> {
            addCategory(categoryName.getText().toString());
            dialog.dismiss();
        });
    }

    private void addCategory(String text) {
        FirebaseDatabase.getInstance().getReference("categoriesBudget").child(Objects.requireNonNull(utils.getAuth().getUid())).child(text).setValue(text);
        loadCategory();
    }
    private void loadCategory(){
        Query query = FirebaseDatabase.getInstance().getReference("categoriesBudget").child(Objects.requireNonNull(utils.getAuth().getUid()));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                categories.add("Select item");
                categories.add("Salary");
                categories.add("Freelancing");
                categories.add("Other");
                categories.add("New category");
                categoriesNew.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Object obj = ds.getValue();
                    assert obj != null;
                    categoriesNew.add(obj.toString());
                }
                categories.remove(categories.size() - 1);
                categories.addAll(categoriesNew);
                categories.add("New category");
                if(spinnerArrayAdapter!=null)
                 spinnerArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(utils.getBudgetRef(), Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @SuppressLint("DefaultLocale")
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int i, @NonNull Data model) {
                if(model.isRecurent()) {
                    holder.setItemAmount("$" + String.format("%.2f",model.getRecurentSum()));
                    holder.nrMonth.setVisibility(View.VISIBLE);
                    holder.nrMonth.setText(model.getNrMonth()-model.getNr()+" month");
                }
                else {
                    holder.setItemAmount("$" + String.format("%.2f",model.getAmount()));
                    holder.nrMonth.setVisibility(View.GONE);
                }
                holder.setDate(model.getDate());
                holder.setItemName(model.getItem());
                switch (model.getItem()){
                    case "Transport":
                        holder.imageView.setImageResource(R.drawable.ic_transport);
                        break;
                    case "Food":
                        holder.imageView.setImageResource(R.drawable.ic_food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.ic_house);
                        break;
                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.ic_entertainment);
                        break;
                    case "Education":
                        holder.imageView.setImageResource(R.drawable.ic_education);
                        break;
                    case "Charity":
                        holder.imageView.setImageResource(R.drawable.ic_consultancy);
                        break;
                    case "Apparel":
                        holder.imageView.setImageResource(R.drawable.ic_shirt);
                        break;
                    case "Health":
                        holder.imageView.setImageResource(R.drawable.ic_health);
                        break;
                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.ic_personalcare);
                        break;
                    case "Other":
                        holder.imageView.setImageResource(R.drawable.ic_other);
                        break;
                }
                holder.notes.setVisibility(View.GONE);
                if(userEmail.getEmail()==null) {
                    holder.mView.setOnClickListener(view -> {
                        post_key = getRef(holder.getAdapterPosition()).getKey();
                        item = model.getItem();
                        amount = model.getAmount();
                        updateData();
                    });
                }
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout,null);

        myDialog.setView(mView);

        final AlertDialog dialog = myDialog.create();

        final Spinner itemSpinner = mView.findViewById(R.id.itemsspinner);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);
        final CheckBox monthlyCheckbox = mView.findViewById(R.id.monthlyCheckbox);
        final EditText numberMonth = mView.findViewById(R.id.numberMonth);
        mNotes.setVisibility(View.GONE);
        monthlyCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(monthlyCheckbox.isChecked()){
                numberMonth.setVisibility(View.VISIBLE);
            }else{
                numberMonth.setVisibility(View.GONE);
            }
        });
        spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        itemSpinner.setAdapter(spinnerArrayAdapter);
        int i=-1;
        for(String s:categories){
            ++i;
            if(s.equals(item)){
                break;
            }
        }
        itemSpinner.setSelection(i);
        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button delBtn = mView.findViewById(R.id.btnDelete);
        Button btnUpdate = mView.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(view -> {
            amount = Double.parseDouble(mAmount.getText().toString());
            if(monthlyCheckbox.isChecked()) {
                utils.updateBudget(BudgetActivity.this,itemSpinner.getSelectedItem().toString(), null, post_key, amount, monthlyCheckbox.isChecked(),Integer.parseInt(numberMonth.getText().toString()),0,amount);
            }else{
                utils.updateBudget(BudgetActivity.this, itemSpinner.getSelectedItem().toString(), null, post_key, amount, monthlyCheckbox.isChecked(),0,0,0);
            }
            dialog.dismiss();
        });

        delBtn.setOnClickListener(view -> {
            utils.delBudget(BudgetActivity.this,post_key);
            dialog.dismiss();
        });
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.e("itemSelected",itemSpinner.getSelectedItem().toString());
                if(itemSpinner.getSelectedItem().toString().equals("New category")){
                    addCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        dialog.show();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageView imageView;
        public TextView notes,nrMonth;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.note);
            nrMonth = itemView.findViewById(R.id.nrMonth);
        }
        public void setItemName(String itemName){
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount(String itemAmount){
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
            amount.setBackgroundColor(getResources().getColor(R.color.green));
        }

        public void setDate(String itemDate){
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }
    }
}