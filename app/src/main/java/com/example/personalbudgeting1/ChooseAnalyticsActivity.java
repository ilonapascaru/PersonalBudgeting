package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ChooseAnalyticsActivity extends AppCompatActivity {
    private Utils utils;
    private TextView totalAmountSpentOn;
    private ArrayList<String> categories =new ArrayList<>();
    private ArrayList<String> categoriesNew = new ArrayList<>();
    private ArrayAdapter<String> spinnerArrayAdapter;
    private Spinner itemSpinner;
    private int choice = 1;
     @SuppressLint("SimpleDateFormat")
     DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
     Calendar cal = Calendar.getInstance();
     String date = dateFormat.format(cal.getTime());
    ArrayList  barArraylist = new ArrayList();
    ArrayList  barArraylist1 = new ArrayList();
    private PieChart pieChart;
    private BarChart barChart,barChart1;
    private String cat="";
    int weeks,months;
    ArrayList<String> m = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_analytics);
        weeks = cal.get(cal.WEEK_OF_YEAR);
        months = cal.get(cal.MONTH);
        Toolbar toolbar = findViewById(R.id.toolbar);
        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        utils = Utils.getInstance();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Analytics");
        utils.getExpensesRef().orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double amount=0.0;
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    amount+=data.getAmount();
                }
                totalAmountSpentOn.setText("Total Day's Spending: $"+String.format("%.2f",amount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                int i=-1;
                for(String s:categoriesNew){
                    ++i;
                    myEdit.putInt("size",categoriesNew.size());
                    myEdit.putString("category"+i,s);
                    myEdit.apply();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        for(int i=0;i<sharedPreferences.getInt("size",0);++i){
            categories.add(sharedPreferences.getString("category"+i,""));
        }
        barChart = findViewById(R.id.barchart);
        barChart1 = findViewById(R.id.barchart1);
        pieChart = findViewById(R.id.pieChart_view);
        dayPie();
        daySavingsGraph();
        itemSpinner = findViewById(R.id.itemsspinner);
        spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        itemSpinner.setAdapter(spinnerArrayAdapter);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cat = itemSpinner.getSelectedItem().toString();
                ((TextView)adapterView.getChildAt(0)).setTypeface(null, Typeface.BOLD);
                if(choice==1)
                    dayGraph(itemSpinner.getSelectedItem().toString());
                else if(choice==2){
                    weekGraph(cat);
                }else if(choice==3){
                    monthGraph(cat);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        DatabaseReference msg = FirebaseDatabase.getInstance().getReference("economisire");
        msg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                   Msg msg = ds.getValue(Msg.class);
                   m.add(msg.getMsg());
                   //utils.showDialog(msg.getMsg(),ChooseAnalyticsActivity.this);
                }
                show(m);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void show(ArrayList<String> msg){
        Random rand = new Random();
        int poz = rand.nextInt(msg.size());
        utils.showDialog(msg.get(poz),ChooseAnalyticsActivity.this);
    }
    ArrayList<PieEntry> pieEntries = new ArrayList<>();
    private void showPieChart(double amount,String s){
        String label = "type";
        //input data and fit data into pie chart entry

        pieEntries.add(new PieEntry((float) amount,s));

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
    private void dayPie()  {
        pieEntries.clear();
        for(String s:categories){
            Query query = utils.getExpensesRef().orderByChild("item").equalTo(s);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        if(data.getDate().equals(date)) {
                            amount += data.getAmount();
                        }
                    }
                    if(amount!=0){
                        showPieChart(amount,s);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void weekPie() {
        pieEntries.clear();
        for(String s:categories){
            Query query = utils.getExpensesRef().orderByChild("item").equalTo(s);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        if(data.getWeek() == weeks)
                            amount+=data.getAmount();
                    }
                    if(amount!=0.0)
                        showPieChart(amount,s);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void monthPie() {
        pieEntries.clear();
        for(String s:categories){
            Query query = utils.getExpensesRef().orderByChild("item").equalTo(s);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        if(data.getMonth() == months)
                            amount+=data.getAmount();
                    }
                    if(amount!=0){
                        showPieChart(amount,s);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void showBarChart(double amount,float i){
        barArraylist.add(new BarEntry(i, (float) amount));
        BarDataSet barDataSet = new BarDataSet(barArraylist,"Value");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        //color bar data set
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        //text color
        barDataSet.setValueTextColor(Color.BLACK);
        //settting text size
        barDataSet.setValueTextSize(16f);
        barChart.getDescription().setEnabled(true);
    }
    private void showBarChart1(double savings,double expenses,float i){
        barArraylist1.add(new BarEntry(i, (float)(savings-expenses)));
        BarDataSet barDataSet = new BarDataSet(barArraylist1,"Value");
        BarData barData = new BarData(barDataSet);
        barChart1.setData(barData);
        //color bar data set
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        //text color
        barDataSet.setValueTextColor(Color.BLACK);
        //settting text size
        barDataSet.setValueTextSize(16f);
        barChart1.getDescription().setEnabled(true);
    }
    private void dayGraph(String c){
        barArraylist.clear();
        Query query = utils.getExpensesRef().orderByChild("item").equalTo(c);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String,Double> map = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    map.put(data.getDate(),map.getOrDefault(data.getDate(),0.0)+data.getAmount());
                }
                AtomicInteger i= new AtomicInteger(-1);
                map.forEach((key,value)->{
                    i.incrementAndGet();
                    showBarChart(value,i.get());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void weekGraph(String c){
        barArraylist.clear();
        Query query = utils.getExpensesRef().orderByChild("item").equalTo(c);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<Integer,Double> map = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    map.put(data.getWeek(),map.getOrDefault(data.getWeek(),0.0)+data.getAmount());
                }
                AtomicInteger i= new AtomicInteger(-1);
                map.forEach((key,value)->{
                    i.incrementAndGet();
                    showBarChart(value,i.get());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void monthGraph(String c){
        barArraylist.clear();
        Query query = utils.getExpensesRef().orderByChild("item").equalTo(c);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<Integer,Double> map = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    map.put(data.getMonth(),map.getOrDefault(data.getMonth(),0.0)+data.getAmount());
                }
                AtomicInteger i= new AtomicInteger(-1);
                map.forEach((key,value)->{
                    i.incrementAndGet();
                    showBarChart(value,i.get());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void daySavingsGraph(){
        barArraylist1.clear();
        utils.getBudgetRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String,Double> mapB = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data =ds.getValue(Data.class);
                    mapB.put(data.getDate(),mapB.getOrDefault(data.getDate(),0.0)+data.getAmount());
                }
                utils.getExpensesRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String,Double> mapE = new HashMap<>();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            Data data = ds.getValue(Data.class);
                            mapE.put(data.getDate(),mapE.getOrDefault(data.getDate(),0.0)+ data.getAmount());
                        }
                        if(mapB.size()>mapE.size()){
                            AtomicInteger i= new AtomicInteger(-1);
                            mapB.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(value,mapE.getOrDefault(key,0.0),i.get());
                            });
                        }else{
                            AtomicInteger i= new AtomicInteger(-1);
                            mapE.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(mapB.getOrDefault(key,0.0),value,i.get());
                            });
                        }
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
    private void weekSavingsGraph(){
        barArraylist1.clear();
        utils.getBudgetRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<Integer,Double> mapB = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data =ds.getValue(Data.class);
                    mapB.put(data.getWeek(),mapB.getOrDefault(data.getWeek(),0.0)+data.getAmount());
                }
                utils.getExpensesRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<Integer,Double> mapE = new HashMap<>();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            Data data = ds.getValue(Data.class);
                            mapE.put(data.getWeek(),mapE.getOrDefault(data.getWeek(),0.0)+ data.getAmount());
                        }
                        if(mapB.size()>mapE.size()){
                            AtomicInteger i= new AtomicInteger(-1);
                            mapB.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(value,mapE.getOrDefault(key,0.0),i.get());
                            });
                        }else{
                            AtomicInteger i= new AtomicInteger(-1);
                            mapE.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(mapB.getOrDefault(key,0.0),value,i.get());
                            });
                        }
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
    private void monthSavingsGraph(){
        barArraylist1.clear();
        utils.getBudgetRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<Integer,Double> mapB = new HashMap<>();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Data data =ds.getValue(Data.class);
                    mapB.put(data.getMonth(),mapB.getOrDefault(data.getMonth(),0.0)+data.getAmount());
                }
                utils.getExpensesRef().orderByChild("date").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<Integer,Double> mapE = new HashMap<>();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            Data data = ds.getValue(Data.class);
                            mapE.put(data.getMonth(),mapE.getOrDefault(data.getMonth(),0.0)+ data.getAmount());
                        }
                        if(mapB.size()>mapE.size()){
                            AtomicInteger i= new AtomicInteger(-1);
                            mapB.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(value,mapE.getOrDefault(key,0.0),i.get());
                            });
                        }else{
                            AtomicInteger i= new AtomicInteger(-1);
                            mapE.forEach((key,value)->{
                                i.incrementAndGet();
                                showBarChart1(mapB.getOrDefault(key,0.0),value,i.get());
                            });
                        }
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
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_analytics,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.daily){
            choice = 1;
            utils.getExpensesRef().orderByChild("date").equalTo(date).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        amount+=data.getAmount();
                    }
                    totalAmountSpentOn.setText("Total Day's Spending: $"+String.format("%.2f",amount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            dayPie();
            dayGraph(cat);
            daySavingsGraph();
        }
        if(item.getItemId() == R.id.weekly){
            choice = 2;
            utils.getExpensesRef().orderByChild("week").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        amount+=data.getAmount();
                    }
                    totalAmountSpentOn.setText("Total Week's Spending: $"+String.format("%.2f",amount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
           weekPie();
           weekGraph(cat);
           weekSavingsGraph();
        }
        if(item.getItemId() == R.id.monthly){
            choice = 3;
            utils.getExpensesRef().orderByChild("month").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double amount=0.0;
                    for(DataSnapshot ds:snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        amount+=data.getAmount();
                    }
                    totalAmountSpentOn.setText("Total Month's Spending: $"+String.format("%.2f",amount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            monthPie();
            monthGraph(cat);
            monthSavingsGraph();
        }
        return super.onOptionsItemSelected(item);
    }
}