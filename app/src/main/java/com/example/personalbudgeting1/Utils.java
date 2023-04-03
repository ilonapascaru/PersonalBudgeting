package com.example.personalbudgeting1;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Utils {
    private static Utils utils=null;
    private FirebaseAuth auth;
    public ProgressDialog progressDialog;


    private DatabaseReference budgetRef,expensesRef,userRef;
    public DatabaseReference getUserRef() {
        return userRef;
    }
    public void setUserRef(DatabaseReference userRef) {
        this.userRef = userRef;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public DatabaseReference getExpensesRef() {
        return expensesRef;
    }

    public DatabaseReference getBudgetRef() {
        return budgetRef;
    }
    private Utils(){
        auth=FirebaseAuth.getInstance();
    }
    public static Utils getInstance(){
        if(utils==null) {

            utils = new Utils();
        }
        return utils;
    }
    public void startIntent(Context context, Object activity,String extra,String name){
        Intent intent = new Intent(context, (Class<?>) activity);
        if(extra != null)
        {
            intent.putExtra(name,extra);
        }
        context.startActivity(intent);
    }
    public synchronized void loadingDialog(Context context,String message)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
    public void login(Context context,String email,String password){
        loadingDialog(context,"login in progress");
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
                expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(auth.getCurrentUser().getUid());
                startIntent(context,MainActivity.class,"email","email");
                ((Activity)context).finish();
            }else{
                Toast.makeText(context,"Email or password is wrong",Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });
    }
    public void showDialog(String text,Context context){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(context,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View myView = inflater.inflate(R.layout.mesaj,null);
        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();
        final TextView msg = myView.findViewById(R.id.msg);
        msg.setText(text);
        dialog.show();
    }
    public void register(Context context,String email,String password,String rcode){
        auth=FirebaseAuth.getInstance();
        loadingDialog(context,"register in progress");
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
                expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(auth.getCurrentUser().getUid());
                if(rcode.equals("")){
                    //save user
                    String code = auth.getCurrentUser().getUid().substring(0,5);
                    saveUser("admin",email, Objects.requireNonNull(auth.getCurrentUser()).getUid(),code);
                    startIntent(context,MainActivity.class,"email","email");
                    ((Activity) context).finish();
                }else{

                    ArrayList<String> codes = new ArrayList<>();
                    userRef = FirebaseDatabase.getInstance().getReference().child("Users");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            codes.clear();
                            for(DataSnapshot ds:snapshot.getChildren()){
                                User user = ds.getValue(User.class);
                                assert user != null;
                                codes.add(user.getCode());
                            }
                            int t=0;
                            for(String cod : codes){
                                if(cod.equals(rcode)){
                                    saveUser("member", email,auth.getCurrentUser().getUid(),rcode);
                                    t=1;
                                    startIntent(context,MainActivity.class,"email","email");
                                    ((Activity) context).finish();
                                    break;
                                }
                            }
                            if(t==0) {
                                auth.getCurrentUser().delete();
                                Toast.makeText(context,"Your member code is invalid!",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }else{
                Toast.makeText(context,"User is already register",Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });
    }
    public void resetPass(Context context,String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, "We have sent you instructions to reset your password!", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void saveUser(String role,String email,String uid,String code){
            User user = new User(role,code,email,uid,0.0,0.0);
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            userRef.setValue(user);
    }
    public synchronized void saveBudget(Context context,String budgetItem,String notes,double amount,boolean isRecurent,int nrMonth,int nr,double recurentSum,String dateTime)
    {
        auth = FirebaseAuth.getInstance();
        if(progressDialog==null)
            loadingDialog(context,"adding a budget item");
        int weeks,months;
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        if(dateTime.equals("")){
            dateTime = date;
        }
        Date date1= null;
        try {
            String id = budgetRef.push().getKey();
            date1 = dateFormat.parse(dateTime);
            cal.setTime(date1);
            weeks = cal.get(cal.WEEK_OF_YEAR);
            months = cal.get(cal.MONTH);
            Data data = new Data(budgetItem,dateTime,id,notes,amount,months,weeks,isRecurent,nrMonth,nr,recurentSum);
            assert id != null;
            budgetRef.child(id).setValue(data).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    userRef.get().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            DataSnapshot ds = task1.getResult();
                            User user = ds.getValue(User.class);
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("budget",amount+user.getBudget());
                            userRef.updateChildren(map);
                        }
                    });
                }else{
                    Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void updateBudget(Context context,String budgetItem,String notes,String id,double amount,boolean isRecurent,int nrMonth,int nr,double recurentSum){
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        int weeks,months;
        weeks = cal.get(cal.WEEK_OF_YEAR);
        months = cal.get(cal.MONTH);
        Data data = new Data(budgetItem,date,id,notes,amount,months, weeks,isRecurent,nrMonth,nr,recurentSum);
        budgetRef.child(id).setValue(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                userRef.get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        DataSnapshot ds = task1.getResult();
                        User user = ds.getValue(User.class);
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("budget",amount+user.getBudget());
                        userRef.updateChildren(map);
                    }
                });
                Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void delBudget(Context context,String id)
    {
        budgetRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Data data = snapshot.getValue(Data.class);
                //Log.e("test123", String.valueOf(data.getAmount()));
                SharedPreferences sharedPreferences =context.getSharedPreferences("MySharedPref",MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putFloat("amountDelBudget", (float) data.getAmount());
                myEdit.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        SharedPreferences sharedPreferences =context.getSharedPreferences("MySharedPref",MODE_PRIVATE);
        budgetRef.child(id).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.e("test123",String.valueOf(sharedPreferences.getFloat("amountDelBudget",0f)));
                 userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         User user = snapshot.getValue(User.class);
                         Map<String,Object> map = new HashMap<>();
                         map.put("budget",user.getBudget()-sharedPreferences.getFloat("amountDelBudget",0f));
                         userRef.updateChildren(map);
                         sharedPreferences.edit().remove("amountDelBudget").apply();
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
                Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    public synchronized void expensesBudget(Context context,String budgetItem,String notes,double amount,boolean isRecurent,int nrMonth,int nr,double recurentSum,String dateTime)
    {
        auth = FirebaseAuth.getInstance();
        if(progressDialog==null)
            loadingDialog(context,"adding a budget item");
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Calendar cal = Calendar.getInstance();
            String date = dateFormat.format(cal.getTime());
             int weeks,months;
            if(dateTime.equals("")){
                dateTime=date;
            }
            Date date1= null;
        try {
            String id = expensesRef.push().getKey();
            date1=dateFormat.parse(dateTime);
            cal.setTime(date1);
            weeks = cal.get(cal.WEEK_OF_YEAR);
            months = cal.get(cal.MONTH);
            Data data = new Data(budgetItem, dateTime, id, notes, amount, months, weeks, isRecurent, nrMonth, nr, recurentSum);
            assert id != null;
            expensesRef.child(id).setValue(data).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    userRef.get().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            DataSnapshot ds = task1.getResult();
                            User user = ds.getValue(User.class);
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("expenses",amount+user.getExpenses());
                            userRef.updateChildren(map);
                        }
                    });
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void updateExpenses(String budgetItem, String notes, String id, double amount, boolean isRecurent, int nrMonth, int nr,double recurentSum){
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        int weeks,months;
        weeks = cal.get(cal.WEEK_OF_YEAR);
        months = cal.get(cal.MONTH);
        Data data = new Data(budgetItem,date,id,notes,amount,months,weeks,isRecurent,nrMonth,nr,recurentSum);
        expensesRef.child(id).setValue(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                userRef.get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("expenses",amount);
                        userRef.updateChildren(map);
                    }
                });
            }
        });
    }
    public void delExpenses(Context context,String id)
    {
        expensesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Data data = snapshot.getValue(Data.class);
                //Log.e("test123", String.valueOf(data.getAmount()));
                SharedPreferences sharedPreferences =context.getSharedPreferences("MySharedPref1",MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putFloat("amountDelBudget", (float) data.getAmount());
                myEdit.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        SharedPreferences sharedPreferences =context.getSharedPreferences("MySharedPref1",MODE_PRIVATE);
        expensesRef.child(id).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Map<String,Object> map = new HashMap<>();
                        map.put("expenses",user.getExpenses()-sharedPreferences.getFloat("amountDelBudget",0f));
                        userRef.updateChildren(map);
                        sharedPreferences.edit().remove("amountDelBudget").apply();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setBudgetRef(DatabaseReference budgetRef) {
        this.budgetRef = budgetRef;
    }

    public void setExpensesRef(DatabaseReference expensesRef) {
        this.expensesRef = expensesRef;
    }
}
