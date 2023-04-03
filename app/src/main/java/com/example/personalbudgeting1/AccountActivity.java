package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountActivity extends AppCompatActivity {
    private Button addMember,selectDate;
    private Utils utils;
    private MemberAdapter adapter;
    private List<User> myUserList;
    private User currentUser;
    private SwitchCompat notificationSwitch;
    private DatePickerDialog.OnDateSetListener setListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        selectDate = findViewById(R.id.selectDate);
        utils = Utils.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView userEmail = findViewById(R.id.userEmail);
        addMember = findViewById(R.id.addMember);
        RecyclerView recyclerView = findViewById(R.id.usersList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        myUserList = new ArrayList<>();
        adapter = new MemberAdapter(this,myUserList);
        recyclerView.setAdapter(adapter);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Account");
        userEmail.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
        utils.getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if(user.getRole().equals("member")){
                    addMember.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        addMember.setOnClickListener(view -> {
           //add new member
            //send email with code
            AlertDialog.Builder myDialog = new AlertDialog.Builder(AccountActivity.this,R.style.dialog);
            LayoutInflater inflater = LayoutInflater.from(AccountActivity.this);
            View myView = inflater.inflate(R.layout.email_layout,null);
            myDialog.setView(myView);
            final AlertDialog dialog = myDialog.create();
            final EditText toEmail = myView.findViewById(R.id.toEmail);
            final EditText subject = myView.findViewById(R.id.subject);
            final Button sendBtn = myView.findViewById(R.id.sendBtn);
            sendBtn.setOnClickListener(view1 -> {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ toEmail.getText().toString()});
                email.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
                email.putExtra(Intent.EXTRA_TEXT,"Use this code "+ Objects.requireNonNull(utils.getAuth().getCurrentUser()).getUid().substring(0,5));

                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                dialog.dismiss();
            });
            dialog.show();
        });
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(utils.getAuth().getCurrentUser()).getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               currentUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        query = FirebaseDatabase.getInstance().getReference().child("Users");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUserList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if(!user.getUid().equals(currentUser.getUid())&&user.getCode().equals(currentUser.getCode())){
                        myUserList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        selectDate.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AccountActivity.this,android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    setListener,year,month,day
            );
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });
        SharedPreferences sharedPreferences = getSharedPreferences("save",MODE_PRIVATE);
        notificationSwitch.setChecked(sharedPreferences.getBoolean("value",false));
        if(notificationSwitch.isChecked()){
            FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(utils.getAuth().getUid()));
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Objects.requireNonNull(utils.getAuth().getUid()));
        }
        notificationSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences.Editor editor = getSharedPreferences("save",MODE_PRIVATE).edit();
            editor.putBoolean("value",b);
            editor.apply();
            if(b){
                FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(utils.getAuth().getUid()));
            }else{
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Objects.requireNonNull(utils.getAuth().getUid()));
            }
        });
        setListener = (datePicker, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date = day+"-"+ month1 +"-"+ year1;
            if(notificationSwitch.isChecked()){
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Extras/"+utils.getAuth().getUid());
                HashMap<String,String> map = new HashMap<>();
                map.put("date",date);
                map.put("topic",Objects.requireNonNull(utils.getAuth().getUid()));
                ref.setValue(map);
            }
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        utils.startIntent(this,MainActivity.class,"email","email");
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        utils.startIntent(this,MainActivity.class,"email","email");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout){
            new AlertDialog.Builder(AccountActivity.this)
                    .setTitle("Personal Budgeting App")
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        FirebaseAuth.getInstance().signOut();
                        utils.startIntent(AccountActivity.this,MainActivity.class,"email","email");
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}