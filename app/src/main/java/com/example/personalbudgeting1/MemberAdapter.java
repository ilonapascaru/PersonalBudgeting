package com.example.personalbudgeting1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyViewHolder> {
    private final Context context;
    private final List<User> myUserList;
    private final Utils utils;

    public MemberAdapter(Context context, List<User> myUserList) {
        this.context = context;
        this.myUserList = myUserList;
        utils = Utils.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final User user = myUserList.get(position);
        holder.email.setText(user.getEmail());
        holder.itemView.setOnClickListener(view -> {
            utils.setExpensesRef(FirebaseDatabase.getInstance().getReference("expenses").child(user.getUid()));
            utils.setBudgetRef(FirebaseDatabase.getInstance().getReference().child("budget").child(user.getUid()));
            UserEmail userEmail = UserEmail.getInstance();
            userEmail.setEmail(user.getEmail());
            Intent intent = new Intent(context,MainActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return myUserList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private final TextView email;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            email  = itemView.findViewById(R.id.email);
        }
    }
}
