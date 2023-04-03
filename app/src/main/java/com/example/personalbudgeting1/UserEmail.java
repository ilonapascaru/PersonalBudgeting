package com.example.personalbudgeting1;

public class UserEmail {
    private static UserEmail userEmail = null;
    private String email;
    private UserEmail(){
    }
    public static UserEmail getInstance(){
        if(userEmail==null){
            userEmail = new UserEmail();
        }
        return userEmail;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail(){
        return email;
    }
}
