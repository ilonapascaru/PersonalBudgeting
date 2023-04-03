package com.example.personalbudgeting1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class TodayItemsAdapter extends RecyclerView.Adapter<TodayItemsAdapter.ViewHolder>{

    private final Context context;
    private final List<Data> myDataList;
    private String post_key = "";
    private String item = "";
    private String note = "";
    private double amount = 0.0;
    private final Utils utils;
    private UserEmail userEmail = UserEmail.getInstance();
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayList<String> categories;
    private LinkedHashSet<String> categoriesNew;
    private Spinner itemSpinner;
    public TodayItemsAdapter(Context context, List<Data> myDataList) {
        this.context = context;
        this.myDataList = myDataList;
        utils = Utils.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.retrieve_layout,parent,false);
        return new ViewHolder(view);

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Data data = myDataList.get(position);
        holder.amount.setBackgroundColor(Color.RED);
        holder.item.setText(data.getItem());
        if(data.isRecurent()) {
            holder.amount.setText("$" + data.getRecurentSum());
            holder.nrMonth.setVisibility(View.VISIBLE);
            holder.nrMonth.setText(data.getNrMonth()-data.getNr()+" month");
        }
        else {
            holder.amount.setText("$" + String.format("%.2f",data.getAmount()));
            holder.nrMonth.setVisibility(View.GONE);
        }
        holder.date.setText(data.getDate());
        holder.notes.setText(data.getNotes());

        switch (data.getItem()){
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
        if(userEmail.getEmail()==null) {
            holder.itemView.setOnClickListener(view -> {
                post_key = data.getId();
                item = data.getItem();
                amount = data.getAmount();
                note = data.getNotes();
                if (!context.getClass().getSimpleName().equals("HistoryActivity"))
                    updateData();
            });
        }
    }
    int i=-1;
    private void getPos(ArrayList<String> categories){
        i=-1;
        for(String s:categories){
            ++i;
            if(s.equals(item)){
                break;
            }
        }
        Log.e("test123", String.valueOf(i));
        itemSpinner.setSelection(i);
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
                getPos(categories);
                categories.add("New category");
                if(spinnerArrayAdapter!=null)
                    spinnerArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void updateData() {
        categories= new ArrayList<>();
        categoriesNew = new LinkedHashSet<>();
        AlertDialog.Builder myDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View mView = inflater.inflate(R.layout.update_layout,null);

        myDialog.setView(mView);

        final AlertDialog dialog = myDialog.create();
        itemSpinner = mView.findViewById(R.id.itemsspinner);
       // final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);
        final CheckBox monthlyCheckbox = mView.findViewById(R.id.monthlyCheckbox);
        final EditText numberMonth = mView.findViewById(R.id.numberMonth);
        mNotes.setVisibility(View.VISIBLE);
        //mItem.setText(item);
        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        mNotes.setText(note);
        mNotes.setSelection(note.length());

        Button delBtn = mView.findViewById(R.id.btnDelete);
        Button btnUpdate = mView.findViewById(R.id.btnUpdate);
        monthlyCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(monthlyCheckbox.isChecked()){
                numberMonth.setVisibility(View.VISIBLE);
            }else{
                numberMonth.setVisibility(View.GONE);
            }
        });
        spinnerArrayAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, categories);
        itemSpinner.setAdapter(spinnerArrayAdapter);
        loadCategory();
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
        btnUpdate.setOnClickListener(view -> {
            note = mNotes.getText().toString();
            amount = Double.parseDouble(mAmount.getText().toString());
            if(monthlyCheckbox.isChecked()) {
                    utils.updateExpenses(itemSpinner.getSelectedItem().toString(), note, post_key, amount, monthlyCheckbox.isChecked(), Integer.parseInt(numberMonth.getText().toString()), 0, amount);
            }else{
                utils.updateExpenses(itemSpinner.getSelectedItem().toString(), note, post_key, amount, monthlyCheckbox.isChecked(),0,0,0);
            }
            dialog.dismiss();
        });

        delBtn.setOnClickListener(view -> {
            utils.delExpenses(context,post_key);
            dialog.dismiss();
        });
        dialog.show();
    }
    private void addCategory() {
        EditText categoryName;
        Button addCategory;
        androidx.appcompat.app.AlertDialog.Builder myDialog = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
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
    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView item, amount, date,notes,nrMonth;
        public ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            notes = itemView.findViewById(R.id.note);
            imageView = itemView.findViewById(R.id.imageView);
            nrMonth = itemView.findViewById(R.id.nrMonth);
        }
    }
}
