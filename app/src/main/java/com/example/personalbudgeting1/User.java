package com.example.personalbudgeting1;

public class User {
    private String role;
    private String code;
    private String email;
    private String uid;
    private double budget;
    private double expenses;

    public User(){

    }
    public User(String role,String code,String email,String uid,double budget,double expenses) {
        this.role = role;
        this.code = code;
        this.email = email;
        this.uid = uid;
        this.budget = budget;
        this.expenses = expenses;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getBudget() {
        return budget;
    }

    public String getEmail(){
        return email;
    }
    public String getUid() {
        return uid;
    }

    public String getRole() {
        return role;
    }

    public String getCode() {
        return code;
    }
}
