package com.example.personalbudgeting1;

public class Data {
    private String item,date,id,notes;
   private int month,week;
   private double amount;
   private boolean isRecurent;
   private int nrMonth;
   private int nr;
   private double recurentSum;
   public Data(){

   }
    public Data(String item, String date, String id, String notes, double amount, int month, int week,boolean isRecurent,int nrMonth,int nr,double recurentSum) {
        this.item = item;
        this.date = date;
        this.id = id;
        this.notes = notes;
        this.amount = amount;
        this.month = month;
        this.week = week;
        this.isRecurent = isRecurent;
        this.nrMonth = nrMonth;
        this.nr=nr;
        this.recurentSum = recurentSum;
    }

    public double getRecurentSum() {
        return recurentSum;
    }

    public void setRecurentSum(double recurentSum) {
        this.recurentSum = recurentSum;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getNrMonth() {
        return nrMonth;
    }

    public void setNrMonth(int nrMonth) {
        this.nrMonth = nrMonth;
    }

    public boolean isRecurent() {
        return isRecurent;
    }

    public void setRecurent(boolean recurent) {
        isRecurent = recurent;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

}
