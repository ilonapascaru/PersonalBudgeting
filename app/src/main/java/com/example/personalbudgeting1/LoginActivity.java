package com.example.personalbudgeting1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.mobiledev.loginlayout.callbacks.ForgotCallBack;
import com.mobiledev.loginlayout.callbacks.LoginCallBack;
import com.mobiledev.loginlayout.callbacks.RegisterCallBack;
import com.mobiledev.loginlayout.fragments.ForgotFragment;
import com.mobiledev.loginlayout.fragments.LoginFragment;
import com.mobiledev.loginlayout.fragments.RegisterFragment;


public class LoginActivity extends AppCompatActivity implements LoginCallBack, RegisterCallBack, ForgotCallBack {
    private FirebaseAuth.AuthStateListener authStateListener;
    private   Utils utils;
    private final LoginFragment loginFragment = new LoginFragment();
    private final ForgotFragment forgotFragment = new ForgotFragment();
    private final RegisterFragment registerFragment = new RegisterFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.login,loginFragment);
        ft.commit();
         utils=Utils.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = utils.getAuth().getCurrentUser();
            if(user!=null)
            {
                utils.setUserRef(FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()));
                utils.setExpensesRef(FirebaseDatabase.getInstance().getReference("expenses").child(user.getUid()));
                utils.setBudgetRef(FirebaseDatabase.getInstance().getReference().child("budget").child(user.getUid()));
                utils.startIntent(LoginActivity.this,MainActivity.class,"email","email");
                finish();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        utils.getAuth().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        utils.getAuth().removeAuthStateListener(authStateListener);
    }
    private int nr =1;
    @Override
    public void onBackPressed() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.login,loginFragment);
        ft.commit();
        if(nr==0){
            nr=1;
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onForgotClicked() {
        nr=0;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.login,forgotFragment);
        ft.commit();
    }
    @Override
    public void onLoginClicked(@NonNull String email, @NonNull String password) {
        utils.login(LoginActivity.this,email,password);
    }

    @Override
    public void onSignUpClicked() {
        nr=0;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.login,registerFragment);
        ft.commit();
    }

    @Override
    public void backButtonClicked() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.login,loginFragment);
        ft.commit();
        nr=1;
    }

    @Override
    public void resetButtonClicked(@NonNull String email) {
        utils.resetPass(LoginActivity.this,email);
    }

    @Override
    public void registerButtonClicked(@NonNull String email, @NonNull String password, @NonNull String memberCode) {
        utils.register(LoginActivity.this,email,password,memberCode);
    }

    @Override
    public int changeButtonResetBackground() {
        return R.drawable.bkg_btn;
    }

    @Override
    public int changeForgotBackground() {
        return 0;
    }

    @Override
    public int changeButtonLoginBackground() {
        return R.drawable.bkg_btn;
    }

    @Override
    public int changeLoginBackground() {
        return 0;
    }

    @Override
    public int changeButtonRegisterBackground() {
        return R.drawable.bkg_btn;
    }

    @Override
    public int changeRegisterBackground() {
        return 0;
    }

    @NonNull
    @Override
    public String changeForgotSubTitle() {
        return null;
    }


    @NonNull
    @Override
    public String changeForgotTitle() {
        return null;
    }

    @NonNull
    @Override
    public String changeLoginSubTitle() {
        return null;
    }

    @NonNull
    @Override
    public String changeLoginTitle() {
        return "Welcome Login";
    }

    @Override
    public int minLengthPassword() {
        return 0;
    }

    @NonNull
    @Override
    public String changeRegisterSubTitle() {
        return null;
    }

    @NonNull
    @Override
    public String changeRegisterTitle() {
        return null;
    }
}