package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class RecuringActivity extends AppCompatActivity {
    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;
    private Utils utils;
    private TodayItemsAdapter adapter;
    private List<Data> myDataList;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private ArrayAdapter<String> adapter1;

    public RecuringActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuring);
        myDataList = new ArrayList<>();
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        utils = Utils.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Recurring");
        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new TodayItemsAdapter(RecuringActivity.this,myDataList);
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab = findViewById(R.id.fab);
        if(!getIntent().getExtras().get("email").equals("email")){
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(view -> addItemSpentOn());
        readRecurringItems();
    }
    private void readRecurringItems() {
        Query query = utils.getExpensesRef().orderByChild("recurent").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                double totalAmount = 0.0;
                if(snapshot.getChildrenCount()>0) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Data data = dataSnapshot.getValue(Data.class);
                        myDataList.add(data);
                        assert data != null;
                        double pTotal = data.getRecurentSum();
                        totalAmount += pTotal;
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    totalAmountSpentOn.setText("Month Recurrent Spending $" + String.format("%.2f", totalAmount));
                }else{
                    progressBar.setVisibility(View.GONE);
                    totalAmountSpentOn.setText("Don't have Recurrent Spending");
                }
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
    Spinner itemSpinner;
    EditText amount;
    EditText note ;
    Button cancel ;
    Button save ;
    Button photoBtn ;
    TextView txt;
    CheckBox monthlyCheckbox;
    EditText numberMonth;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private void addItemSpentOn() {
        loadCategory();
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout,null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        numberMonth = myView.findViewById(R.id.numberMonth);
        itemSpinner = myView.findViewById(R.id.itemsspinner);
        amount = myView.findViewById(R.id.amount);
        note = myView.findViewById(R.id.note);
        cancel = myView.findViewById(R.id.cancel);
        save = myView.findViewById(R.id.save);
        photoBtn = myView.findViewById(R.id.photoBtn);
        txt = myView.findViewById(R.id.txt);
        monthlyCheckbox = myView.findViewById(R.id.monthlyCheckbox);
        txt.setVisibility(View.VISIBLE);
        photoBtn.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
        monthlyCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(monthlyCheckbox.isChecked()){
                numberMonth.setVisibility(View.VISIBLE);
            }else{
                numberMonth.setVisibility(View.GONE);
            }
        });
        txt.setText("Not uploaded");
        adapter1 =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        adapter1.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        itemSpinner.setAdapter(adapter1);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.e("itemSelected",itemSpinner.getSelectedItem().toString());
                if(itemSpinner.getSelectedItem().toString().equals("Categorie noua")){
                    addCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        photoBtn.setOnClickListener(view -> {
            if(!checkCameraPermission()){
                requestCameraPermission();
            }else{
                PickImage();
            }

        });
        save.setOnClickListener(view -> {
            String Amount = amount.getText().toString();
            String Item = itemSpinner.getSelectedItem().toString();
            String notes = note.getText().toString();
            if(TextUtils.isEmpty(Amount)){
                amount.setError("Amount is required!");
                return;
            }
            if(TextUtils.isEmpty(notes)){
                note.setError("Note is required");
                return;
            }
            if(Item.equals("Select item")){
                Toast.makeText(RecuringActivity.this,"Select a valid item",Toast.LENGTH_SHORT).show();

            }else if(monthlyCheckbox.isChecked()){
                utils.expensesBudget(RecuringActivity.this, Item,notes,Double.parseDouble(Amount),monthlyCheckbox.isChecked(),Integer.parseInt(numberMonth.getText().toString()),0,Double.parseDouble(Amount),"");
                dialog.dismiss();
                utils.progressDialog.dismiss();
            }else{
                utils.expensesBudget(RecuringActivity.this, Item,notes,Double.parseDouble(Amount),monthlyCheckbox.isChecked(),0,0,0,"");
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
            //Log.e("categoryName",categoryName.getText().toString());
            addCategory(categoryName.getText().toString());
            dialog.dismiss();
        });
    }

    private void addCategory(String text) {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        categoriesNew.add(text);
        adapter1.notifyDataSetChanged();
        String json = gson.toJson(categoriesNew);
        editor.putString("categories",json);
        editor.apply();
        loadCategory();
    }
    private void loadCategory(){
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
        categories.add("Categorie noua");
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("categories",null);
        Type type = new TypeToken<LinkedHashSet<String>>(){}.getType();
        categoriesNew = gson.fromJson(json,type);
        if(categoriesNew == null)
            categoriesNew = new LinkedHashSet<>();
        categories.remove(categories.size() - 1);
        categories.addAll(categoriesNew);
        categories.add("Categorie noua");
    }
    private void PickImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }
}