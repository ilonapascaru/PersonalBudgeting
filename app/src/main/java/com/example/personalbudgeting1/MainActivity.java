package com.example.personalbudgeting1;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;
import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.personalbudgeting1.history.HistoryActivity;
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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.opencsv.CSVReader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private TextView budgetTv,todayTv,monthTv,savingsTv,fbudgetTv,todayCheltuieli,monthCheltuieli,mCheltuieli,fCheltuieli;
    private float totalAmountBudget;
    private Utils utils;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private DatabaseReference ref;
    private int i1 = -1;
    private final int delay = 2000;
    private ArrayAdapter<String> adapter;
    private  FloatingActionButton fab;
    private final UserEmail userEmail=UserEmail.getInstance();
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fCheltuieli = findViewById(R.id.fCheltuieli);
        mCheltuieli = findViewById(R.id.mCheltuieli);
        monthCheltuieli = findViewById(R.id.monthCheltuieli);
        todayCheltuieli = findViewById(R.id.todayCheltuieli);
        ref = FirebaseDatabase.getInstance().getReference().child("TextBon");
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        LinearLayout categoriesCardView = findViewById(R.id.categoriesCardView);
        LinearLayout recuringCardView = findViewById(R.id.recuringCardView);
        fbudgetTv = findViewById(R.id.fbudgetTv);
        budgetTv = findViewById(R.id.budgetTv);
        todayTv = findViewById(R.id.todayTv);
        monthTv = findViewById(R.id.monthTv);
        savingsTv = findViewById(R.id.savingsTv);
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout budgetCardView = findViewById(R.id.budgetCardView);
        LinearLayout todayCardView = findViewById(R.id.todayCardView);
        LinearLayout analitycsCardView = findViewById(R.id.analitycsCardView);
        LinearLayout historyCardView = findViewById(R.id.historyCardView);
         fab = findViewById(R.id.fab);
        utils = Utils.getInstance();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Personal Budgeting App");
        if(userEmail.getEmail()!=null)
        {
            getSupportActionBar().setTitle(userEmail.getEmail());
            fab.setVisibility(View.GONE);
        }
        categoriesCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this,CategoriesActivity.class,getIntent().getExtras().get("email").toString(),"email"));
        recuringCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this,RecuringActivity.class,getIntent().getExtras().get("email").toString(),"email"));
        fab.setOnClickListener(view -> chooseAction());
        budgetCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this,BudgetActivity.class,getIntent().getExtras().get("email").toString(),"email"));
        todayCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this,TodaySpendingActivity.class,getIntent().getExtras().get("email").toString(),"email"));
        analitycsCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this,ChooseAnalyticsActivity.class,null,null));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            historyCardView.setOnClickListener(view -> utils.startIntent(MainActivity.this, HistoryActivity.class,null,null));
        }
    }
    private void getFamilyExpensesAmount() {
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String code;
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if(utils.getAuth().getCurrentUser()!=null){
                        if(user.getUid().equals(Objects.requireNonNull(utils.getAuth().getCurrentUser()).getUid())) {
                            code = user.getCode();
                            FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("code").equalTo(code).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    float total = 0f;
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        User user1 = ds.getValue(User.class);
                                        assert user1 != null;
                                        total += user1.getExpenses();
                                    }
                                    fCheltuieli.setText("$" + String.format("%.2f", total));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getFamilyBudgetAmount() {
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
           @SuppressLint({"SetTextI18n", "DefaultLocale"})
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               String code;
               for(DataSnapshot ds : snapshot.getChildren()){
                   User user = ds.getValue(User.class);
                   assert user != null;
                   if(utils.getAuth().getCurrentUser()!=null){
                   if(user.getUid().equals(Objects.requireNonNull(utils.getAuth().getCurrentUser()).getUid())) {
                       code = user.getCode();
                       FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("code").equalTo(code).addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               float total = 0f;
                               for (DataSnapshot ds : snapshot.getChildren()) {
                                   User user1 = ds.getValue(User.class);
                                   assert user1 != null;
                                   total += user1.getBudget();
                               }
                               fbudgetTv.setText("$" + String.format("%.2f", total));
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {

                           }
                       });
                   }
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void chooseAction() {
        androidx.appcompat.app.AlertDialog.Builder myDialog = new androidx.appcompat.app.AlertDialog.Builder(this,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.choose_layout,null);
        myDialog.setView(myView);
        final androidx.appcompat.app.AlertDialog dialog = myDialog.create();
        dialog.show();
        Button pdfButton = myView.findViewById(R.id.pdfButton);
        Button manualButton = myView.findViewById(R.id.manualButton);
        Button csvButton = myView.findViewById(R.id.csvButton);
        Button excelButton = myView.findViewById(R.id.excelButton);
        pdfButton.setOnClickListener(view -> {
            choosePDF();
            dialog.dismiss();
        });
        manualButton.setOnClickListener(view -> {
            addItemSpentOn();
            dialog.dismiss();
        });
        csvButton.setOnClickListener(view -> {
            chooseCSV();
            dialog.dismiss();
        });
        excelButton.setOnClickListener(view -> {
            chooseExcel();
            dialog.dismiss();
        });
    }

    private void choosePDF() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent,123);

    }
    private void chooseExcel(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        startActivityForResult(intent, 125);
    }
    InputStream inputStream;
    String nr1="",nr2="";
    private final ArrayList<Double> sums = new ArrayList<>();
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ObsoleteSdkInt")
    private void extractTextPdfFile(Uri uri){
        sums.clear();
        try{
            inputStream = getContentResolver().openInputStream(uri);
            String fileContent;
            StringBuilder builder = new StringBuilder();
            PdfReader reader = null;
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    reader = new PdfReader(inputStream);

                    int pages = reader.getNumberOfPages();
                    for (int i = 1; i <= pages; ++i){
                        fileContent = PdfTextExtractor.getTextFromPage(reader, i);
                        builder.append(fileContent);
                    }
                }
                assert reader != null;
                reader.close();
                int n;
                boolean ok = false;
                StringTokenizer st = new StringTokenizer(String.valueOf(builder),"\n");
                while(st.hasMoreTokens()){
                    String ref = st.nextToken();
                    if(ref.equals("REF")){
                        ok = true;
                        n=0;
                        StringTokenizer st1 = new StringTokenizer(st.nextToken()," ");
                        while(st1.hasMoreTokens()){
                            String nr = st1.nextToken();
                            if(isNumber(nr)){
                                ++n;
                                if(n==1)
                                {
                                    nr1=nr;
                                }else{
                                    sums.add(Double.parseDouble(nr1));
                                    nr2=nr;
                                    sums.add(Double.parseDouble(nr2)*(-1));
                                    n=0;

                                }
                            }

                        }
                    }
                }
                if(!ok){
                    sums.clear();
                    StringTokenizer st1 = new StringTokenizer(String.valueOf(builder)," ");
                    while(st1.hasMoreTokens()){
                       String nrOp = st1.nextToken();
                       if(st1.hasMoreTokens() && st1.nextToken().contains("RON")){

                               nrOp = nrOp.replace(".", "");
                               nrOp = nrOp.replace(",", ".");
                               if(isNumber(nrOp)){
                                   sums.add(Double.parseDouble(nrOp));
                               }

                       }
                    }
                }
                HashMap<Double,Integer> map = new HashMap<>();
                for(double d:sums){
                    map.put(d,map.getOrDefault(d,0)+1);
                }
                if(sums.size()>0) {
                    utils.loadingDialog(this,"adding a budget item");
                    new CountDownTimer((long) delay * sums.size(), delay) { //40000 milli seconds is total time, 1000 milli seconds is time interval

                        public void onTick(long millisUntilFinished) {
                            ++i1;
                            if(map.get(sums.get(i1))>1){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Warning")
                                        .setMessage("Are you sure you want to insert "+sums.get(i1)+"$ ?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Continue with delete operation
                                                if (sums.get(i1) < 0) {
                                                    utils.expensesBudget(MainActivity.this, "Other", "from bank", abs(sums.get(i1)), false, 0, 0, 0,"");
                                                } else {
                                                    utils.saveBudget(MainActivity.this, "Other", null, sums.get(i1), false, 0, 0, 0,"");
                                                }
                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton(android.R.string.no, null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }else{
                                map.put(sums.get(i1),1 );
                                if (sums.get(i1) < 0) {
                                    utils.expensesBudget(MainActivity.this, "Other", "from bank", abs(sums.get(i1)), false, 0, 0, 0,"");
                                } else {
                                    utils.saveBudget(MainActivity.this, "Other", null, sums.get(i1), false, 0, 0, 0,"");
                                }
                            }
                        }

                        public void onFinish() {
                            utils.progressDialog.dismiss();
                            i1=-1;
                        }
                    }.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(utils.getAuth().getCurrentUser()==null)
        {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        if(utils.getBudgetRef()!=null) {
            getBudgetAmount();
            getTodaySpentAmount();
            getMonthSpentAmount();
            getFamilyBudgetAmount();
            getFamilyExpensesAmount();
            getTodayBudgetAmount();
            getMonthBudgetAmount();
            getSpentAmount();
            getSavings();
        }
        if(userEmail.getEmail()!=null)
        {
            Objects.requireNonNull(getSupportActionBar()).setTitle(userEmail.getEmail());
            fab.setVisibility(View.GONE);
        }
    }
    private double budget = 0.0,expenses = 0.0;
    private void getSavings() {
        utils.getBudgetRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                budget = 0.0;
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    budget += data.getAmount();
                }
                utils.getExpensesRef().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        expenses = 0.0;
                        for(DataSnapshot ds:snapshot.getChildren()){
                            Data data = ds.getValue(Data.class);
                            expenses += data.getAmount();
                        }
                        if(budget-expenses<0){
                            savingsTv.setBackgroundColor(getResources().getColor(R.color.red));
                        }else{
                            savingsTv.setBackgroundColor(getResources().getColor(R.color.green));
                        }
                        savingsTv.setText("$ "+String.format("%.2f",budget-expenses));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthSpentAmount() {
        Calendar cal = Calendar.getInstance();
        int months = cal.get(cal.MONTH);

        Query query = utils.getExpensesRef().orderByChild("month").equalTo(months);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalAmount = 0;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total = data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                monthCheltuieli.setText("$ "+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthBudgetAmount() {
        Calendar cal = Calendar.getInstance();
        int months = cal.get(cal.MONTH);
        Query query = utils.getBudgetRef().orderByChild("month").equalTo(months);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalAmount = 0;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total = data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                monthTv.setText("$ "+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTodaySpentAmount() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        Query query = utils.getExpensesRef().orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalAmount = 0f;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total =  data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                todayCheltuieli.setText("$"+String.format("%.2f",totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getTodayBudgetAmount() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        Query query = utils.getBudgetRef().orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalAmount = 0f;
                for (DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    assert data != null;
                    double total =  data.getAmount();
                    float pTotal = Float.parseFloat(String.valueOf(total));
                    totalAmount+=pTotal;

                }
                todayTv.setText("$"+String.format("%.2f",totalAmount));
                //utils.getPersonalRef().child("today").setValue(totalAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getBudgetAmount() {
        utils.getBudgetRef().addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalAmountBudget = 0;
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    getFamilyBudgetAmount();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        assert data != null;
                        double total = data.getAmount();
                        float pTotal = Float.parseFloat(String.valueOf(total));
                        totalAmountBudget += pTotal;
                    }
                    budgetTv.setText("$ "+String.format("%.2f",totalAmountBudget));
                }else {
                    budgetTv.setText("$ 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSpentAmount() {
        utils.getExpensesRef().addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalAmountBudget = 0;
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    getFamilyBudgetAmount();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        assert data != null;
                        double total = data.getAmount();
                        float pTotal = Float.parseFloat(String.valueOf(total));
                        totalAmountBudget += pTotal;
                    }
                    mCheltuieli.setText("$ "+String.format("%.2f",totalAmountBudget));
                    //utils.getPersonalRef().child("budget").setValue(totalAmountBudget);
                }else {
                    mCheltuieli.setText("$ 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.account){
            if(userEmail.getEmail()==null) {
                utils.startIntent(MainActivity.this, AccountActivity.class, null, null);
            }
            else{
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Personal Budgeting App")
                        .setMessage("Are you sure you want to exit from this user?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            utils.setExpensesRef(FirebaseDatabase.getInstance().getReference("expenses").child(Objects.requireNonNull(utils.getAuth().getCurrentUser()).getUid()));
                            utils.setBudgetRef(FirebaseDatabase.getInstance().getReference().child("budget").child(utils.getAuth().getCurrentUser().getUid()));
                            userEmail.setEmail(null);
                            utils.startIntent(MainActivity.this,AccountActivity.class,null,null);
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    Spinner itemSpinner;
    EditText amount;
    EditText note ;
    EditText numberMonth;
    Button cancel ;
    Button save ;
    Button photoBtn ;
    TextView txt;
    CheckBox monthlyCheckbox;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private void addItemSpentOn() {
        loadCategory();
        androidx.appcompat.app.AlertDialog.Builder myDialog = new androidx.appcompat.app.AlertDialog.Builder(this,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout,null);
        myDialog.setView(myView);
        final androidx.appcompat.app.AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        numberMonth = myView.findViewById(R.id.numberMonth);
        monthlyCheckbox = myView.findViewById(R.id.monthlyCheckbox);
        itemSpinner = myView.findViewById(R.id.itemsspinner);
        amount = myView.findViewById(R.id.amount);
        note = myView.findViewById(R.id.note);
        cancel = myView.findViewById(R.id.cancel);
        save = myView.findViewById(R.id.save);
        photoBtn = myView.findViewById(R.id.photoBtn);
        txt = myView.findViewById(R.id.txt);
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
        adapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        itemSpinner.setAdapter(adapter);

        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
                Toast.makeText(MainActivity.this,"Select a valid item",Toast.LENGTH_SHORT).show();

            }else if(monthlyCheckbox.isChecked()){
                utils.expensesBudget(MainActivity.this, Item,notes,Float.parseFloat(Amount),monthlyCheckbox.isChecked(),Integer.parseInt(numberMonth.getText().toString()),0,0,"");
                dialog.dismiss();
            }else{
                utils.expensesBudget(MainActivity.this, Item,notes,Float.parseFloat(Amount),monthlyCheckbox.isChecked(),0,0,0,"");
                dialog.dismiss();
            }
            utils.progressDialog.dismiss();
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
                if(adapter!=null)
                    adapter.notifyDataSetChanged();
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

    @SuppressLint("NewApi")
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
    private final List<AccountData> accountDataList = new ArrayList<>();
    private void chooseCSV() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        startActivityForResult(intent,124);

    }
    private String[] det={"Transfer","Cumparare","Plati","Comision"};
    private String[] food = {"Profi","Kaufland","Lidl"};
    private String[] entertainment = {"Netflix","Hbo"};
    HashMap<Double,Integer> map;
    CountDownTimer countDownTimer;
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void putMap(double d){
        map.put(d,map.getOrDefault(d,0)+1);
    }
    private void readAccountData(Uri uri) throws IOException {
        i1=-1;
        accountDataList.clear();
        InputStream is = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        CSVReader reader1 = new CSVReader(reader);
        String[] line;
        int nr=0;
        while((line=reader1.readNext())!=null){
            ArrayList<String> d = new ArrayList<>();
            for(String s:line){
                if(!s.isEmpty()){
                    d.add(s);
                }
            }
            for(int i=0;i<d.size();++i){
                String t = d.get(i).replace(".", "").replace(",", ".");
                if(isNumber(d.get(i).replace(".","").replace(",","."))){
                    for(String s:det){
                        if(d.get(i-1).contains(s)){
                            nr=1;
                            break;
                        }
                    }
                    if(nr==0) {
                        if(!d.get(i-1).contains("Sold"))
                        {
                            String[] d1 = d.get(i-2).split(" ");
                            switch (d1[1]){
                                case "mai":
                                    d1[1]="05";
                                    break;
                                case "iunie":
                                    d1[1]="06";
                                    break;
                                case "iulie":
                                    d1[1]="07";
                                    break;
                                case "august":
                                    d1[1]="08";
                                    break;
                                case "septembrie":
                                    d1[1]="09";
                                    break;
                                case "octombrie":
                                    d1[1]="10";
                                    break;
                                case "noiembrie":
                                    d1[1]="11";
                                    break;
                                case "decembrie":
                                    d1[1]="12";
                                    break;
                                case "aprilie":
                                    d1[1]="04";
                                    break;
                                case "martie":
                                    d1[1]="03";
                                    break;
                                case "februarie":
                                    d1[1]="02";
                                    break;
                                case "ianuarie":
                                    d1[1]="01";
                                    break;
                            }
                            String date = d1[0]+"-"+d1[1]+"-"+d1[2];
                            accountDataList.add(new AccountData(Double.parseDouble(t),d.get(i-1),date));
                        }
                    }else{
                        nr=0;
                        String[] d1 = d.get(i-2).split(" ");
                        switch (d1[1]){
                            case "mai":
                                d1[1]="05";
                                break;
                            case "iunie":
                                d1[1]="06";
                                break;
                            case "iulie":
                                d1[1]="07";
                                break;
                            case "august":
                                d1[1]="08";
                                break;
                            case "septembrie":
                                d1[1]="09";
                                break;
                            case "octombrie":
                                d1[1]="10";
                                break;
                            case "noiembrie":
                                d1[1]="11";
                                break;
                            case "decembrie":
                                d1[1]="12";
                                break;
                            case "aprilie":
                                d1[1]="04";
                                break;
                            case "martie":
                                d1[1]="03";
                                break;
                            case "februarie":
                                d1[1]="02";
                                break;
                            case "ianuarie":
                                d1[1]="01";
                                break;
                        }
                        String date = d1[0]+"-"+d1[1]+"-"+d1[2];
                        accountDataList.add(new AccountData(-Double.parseDouble(t),d.get(i-1),date));
                    }
                }
            }
        }
        map = new HashMap<>();
        utils.getExpensesRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    putMap(-data.getAmount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        utils.getBudgetRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    putMap(data.getAmount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(accountDataList.size()>0) {
            utils.loadingDialog(this,"adding a budget item");
          countDownTimer= new CountDownTimer((long) delay * (accountDataList.size()), delay) { //40000 milli seconds is total time, 1000 milli seconds is time interval

                @RequiresApi(api = Build.VERSION_CODES.N)
                public void onTick(long millisUntilFinished) {
                    ++i1;
                    if(map.getOrDefault(accountDataList.get(i1).getSuma(),0)>1){
                       pause();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Warning")
                                .setMessage("Are you sure you want to insert "+accountDataList.get(i1).getSuma()+"$ ?")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        resume();
                                        if (accountDataList.get(i1).getSuma() < 0) {
                                            utils.expensesBudget(MainActivity.this, "Other", accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()), false, 0, 0, 0, accountDataList.get(i1).getData());
                                        } else {
                                            utils.saveBudget(MainActivity.this, "Other", null, accountDataList.get(i1).getSuma(), false, 0, 0, 0, accountDataList.get(i1).getData());
                                        }
                                        if(i1==accountDataList.size()-1)
                                        {
                                            pause();
                                            utils.progressDialog.dismiss();
                                            i1=-1;
                                        }
                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(i1==accountDataList.size()-1)
                                        {
                                            pause();
                                            utils.progressDialog.dismiss();
                                        }else
                                            resume();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }else {
                        if (accountDataList.get(i1).getSuma() < 0) {
                                int o = 0;
                                for(String s:food){
                                    if(accountDataList.get(i1).getDetalii().contains(s)){
                                        utils.expensesBudget(MainActivity.this, "Food",
                                                accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                                false, 0, 0, 0,
                                                accountDataList.get(i1).getData().replace(".", "-"));
                                        o=1;
                                        break;
                                    }
                                }
                                for(String s:entertainment){
                                    if(accountDataList.get(i1).getDetalii().contains(s)){
                                        o=1;
                                        utils.expensesBudget(MainActivity.this, "Entertainment",
                                                accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                                false, 0, 0, 0,
                                                accountDataList.get(i1).getData().replace(".", "-"));
                                        break;
                                    }
                                }
                                if(o==0)
                                {
                                    utils.expensesBudget(MainActivity.this, "Other",
                                            accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                            false, 0, 0, 0,
                                            accountDataList.get(i1).getData().replace(".", "-"));

                                }
                        } else {
                            utils.saveBudget(MainActivity.this, "Other", null, accountDataList.get(i1).getSuma(), false, 0, 0, 0, accountDataList.get(i1).getData());
                        }
                    }
                    if(i1==accountDataList.size()-1)
                    {
                        pause();
                        utils.progressDialog.dismiss();
                    }
                }

                public void onFinish() {
                    utils.progressDialog.dismiss();
                    i1=-1;
                }
            }.start();
        }
    }
    private void pause(){
        countDownTimer.cancel();
    }
    private void resume(){
        countDownTimer.start();
    }
    private void extractTextExcelFile(Uri uri) throws IOException {
        accountDataList.clear();
        InputStream is = getContentResolver().openInputStream(uri);
        int nr = 0;
        Workbook workbook = new HSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            List<String> tokensList = new ArrayList<>();
            for (Cell cell : row) {
                if (cell.getCellType() == CELL_TYPE_NUMERIC) {
                    tokensList.add(String.valueOf(cell.getNumericCellValue()));
                }
                if(cell.getCellType() == CELL_TYPE_STRING){
                    tokensList.add(cell.getStringCellValue());
                }
            }
            String[] tokens = tokensList.toArray(new String[tokensList.size()]);
            if (nr == 0) {
                for (String i : tokens) {
                    if (i.equals("Suma / Amount")) {
                        nr = 1;
                        break;
                    }
                }
            } else {
                double sum = Double.parseDouble(tokens[1].replace(".","").replace(",","."));
                if (tokens.length >= 15) {
                    AccountData accountData = new AccountData(sum , tokens[5], tokens[14]);
                    accountDataList.add(accountData);
                } else {
                    AccountData accountData = new AccountData(sum,tokens[5]);
                    accountDataList.add(accountData);
                }
            }
        }
        if (accountDataList.size() > 0) {
            utils.loadingDialog(this, "adding a budget item");
            new CountDownTimer((long) delay * accountDataList.size(), delay) { //40000 milli seconds is total time, 1000 milli seconds is time interval

                public void onTick(long millisUntilFinished) {
                    ++i1;
                    if (accountDataList.get(i1).getSuma() < 0) {
                        if (accountDataList.get(i1).getDetalii() == null)
                            utils.expensesBudget(MainActivity.this, "Other", "from bank", abs(accountDataList.get(i1).getSuma()), false, 0, 0, 0, accountDataList.get(i1).getData().replace(".", "-"));
                        else {
                            int o = 0;
                            for(String s:food){
                                if(accountDataList.get(i1).getDetalii().contains(s)){
                                    utils.expensesBudget(MainActivity.this, "Food",
                                            accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                            false, 0, 0, 0,
                                            accountDataList.get(i1).getData().replace(".", "-"));
                                    o=1;
                                    break;
                                }
                            }
                            for(String s:entertainment){
                                if(accountDataList.get(i1).getDetalii().contains(s)){
                                    o=1;
                                    utils.expensesBudget(MainActivity.this, "Entertainment",
                                            accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                            false, 0, 0, 0,
                                            accountDataList.get(i1).getData().replace(".", "-"));
                                    break;
                                }
                            }
                            if(o==0)
                            {
                                        utils.expensesBudget(MainActivity.this, "Other",
                                                accountDataList.get(i1).getDetalii(), abs(accountDataList.get(i1).getSuma()),
                                                false, 0, 0, 0,
                                                accountDataList.get(i1).getData().replace(".", "-"));

                            }
                        }
                    } else {
                        utils.saveBudget(MainActivity.this, "Other", null,accountDataList.get(i1).getSuma(), false, 0, 0, 0, accountDataList.get(i1).getData().replace(".", "-"));
                    }
                }

                public void onFinish() {
                    utils.progressDialog.dismiss();
                    i1 = -1;
                }
            }.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        if (requestCode == 123){
            if(resultCode == RESULT_OK){
                if (data != null){
                   extractTextPdfFile(data.getData());
                }
            }
        }
        if(requestCode == 124){
            if(resultCode == RESULT_OK){
                if(data != null){
                    try {
                        readAccountData(data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(requestCode == 125){
            if(resultCode == RESULT_OK){
                try{
                    extractTextExcelFile(data.getData());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


}