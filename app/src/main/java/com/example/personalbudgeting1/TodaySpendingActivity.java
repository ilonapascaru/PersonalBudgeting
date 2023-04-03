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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class TodaySpendingActivity extends AppCompatActivity {

    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;
    private Utils utils;
    private TodayItemsAdapter adapter;
    private List<Data> myDataList;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private DatabaseReference ref;
    private  ArrayAdapter<String> adapter1;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_spending);
        ref = FirebaseDatabase.getInstance().getReference().child("TextBon");
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        utils = Utils.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Spendings");
        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        if(!getIntent().getExtras().get("email").equals("email")){
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(view -> addItemSpentOn());
        loadCategory();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        adapter = new TodayItemsAdapter(TodaySpendingActivity.this,myDataList);
        recyclerView.setAdapter(adapter);
        readTodayItems();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void readTodayItems() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        Query query = utils.getExpensesRef().orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                double totalAmount = 0.0;
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                    assert data != null;
                    double pTotal = data.getAmount();
                    totalAmount+=pTotal;
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                totalAmountSpentOn.setText("Total Day's Spending: $"+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
     Spinner itemSpinner;
     EditText amount;
     EditText note ;
     Button cancel ;
     Button save ;
     Button photoBtn ;
     TextView txt;
     CheckBox monthlyCheckbox;
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
        final EditText numberMonth = myView.findViewById(R.id.numberMonth);
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
                if(itemSpinner.getSelectedItem().toString().equals("New category")){
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
                Toast.makeText(TodaySpendingActivity.this,"Select a valid item",Toast.LENGTH_SHORT).show();

            }else{
                if(monthlyCheckbox.isChecked()) {
                    utils.expensesBudget(TodaySpendingActivity.this, Item, notes, Double.parseDouble(Amount), monthlyCheckbox.isChecked(),Integer.parseInt(numberMonth.getText().toString()),0,Double.parseDouble(Amount),"");
                }else{
                    utils.expensesBudget(TodaySpendingActivity.this, Item, notes, Double.parseDouble(Amount), monthlyCheckbox.isChecked(),0,0,0,"");
                }
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
        FirebaseDatabase.getInstance().getReference("categories").child(Objects.requireNonNull(utils.getAuth().getUid())).child(text).setValue(text);
        loadCategory();
    }
    private void loadCategory(){
        Query query = FirebaseDatabase.getInstance().getReference("categories").child(Objects.requireNonNull(utils.getAuth().getUid()));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                if(adapter1!=null)
                    adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
    private boolean isNumber(String nr){
        try{
            Double.parseDouble(nr);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
    private int isOk=1;
    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try{
                    FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(getApplicationContext(),resultUri);
                    FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                            .getOnDeviceTextRecognizer();
                    textRecognizer.processImage(image)
                            .addOnSuccessListener(firebaseVisionText -> {
                                HashMap<String,String> t = new HashMap<>();
                                t.put("textBon",firebaseVisionText.getText());
                                ref.child("text").setValue(t);
                                StringTokenizer st = new StringTokenizer(firebaseVisionText.getText(),"\n");
                                while(st.hasMoreTokens()){
                                    String total = st.nextToken();
                                    if((total.contains("Total")|| total.contains("TOTAL") ||total.contains("SUMA")||total.contains("total"))&&!total.equals("SUBTOTAL")){
                                        StringTokenizer st1 = new StringTokenizer(st.nextToken(),",");
                                        if(st1.countTokens()>1) {
                                            while (st1.hasMoreTokens()) {
                                                String am = st1.nextToken() + "." + st1.nextToken();
                                                am = am.replace("LEI","");
                                                if(isNumber(am)) {
                                                    amount.setText(am);
                                                    txt.setText("Uploaded");
                                                    isOk=0;
                                                }
                                                else{
                                                    txt.setText("Not recognized");
                                                    amount.setText("");
                                                }
                                            }
                                        }else{
                                            String sum = st1.nextToken().replace("LEI","");
                                            if(isNumber(sum)) {
                                                amount.setText(sum);
                                                txt.setText("Uploaded");
                                                isOk=0;
                                            }
                                            else{
                                                txt.setText("Not recognized");
                                                amount.setText("");
                                            }
                                        }
                                        break;
                                    }
                                }
                                if(isOk==1)
                                {
                                    txt.setText("Not recognized");
                                }
                            })
                            .addOnFailureListener(e -> {

                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                txt.setText("Not uploaded");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_spendings,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.today){
            readTodayItems();
        }else if(item.getItemId() == R.id.week){
            readWeekItems();
        }else if(item.getItemId() == R.id.month){
            readMonthItems();
        }
        return super.onOptionsItemSelected(item);
    }

    private void readMonthItems() {
        Calendar cal = Calendar.getInstance();
        int months = cal.get(cal.MONTH);

        Query query = utils.getExpensesRef().orderByChild("month").equalTo(months);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                float totalAmount = 0;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    Object total = data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                totalAmountSpentOn.setText("Total Month's Spending: $"+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readWeekItems() {
        Calendar cal = Calendar.getInstance();
        int weeks = cal.get(cal.WEEK_OF_YEAR);

        Query query = utils.getExpensesRef().orderByChild("week").equalTo(weeks);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                float totalAmount = 0;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total = data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                totalAmountSpentOn.setText("Total Week's Spending: $"+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}