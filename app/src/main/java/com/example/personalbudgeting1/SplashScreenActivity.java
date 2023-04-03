package com.example.personalbudgeting1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    Animation animation;
    private Utils utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        utils = Utils.getInstance();
        animation = AnimationUtils.loadAnimation(this,R.anim.animation);
        ImageView imageView = findViewById(R.id.imageView);
        TextView appName = findViewById(R.id.appName);
        imageView.setAnimation(animation);
        appName.setAnimation(animation);
        int SPLASH = 3000;
        new Handler().postDelayed(() -> {
            utils.startIntent(SplashScreenActivity.this,LoginActivity.class,null,null);
            finish();
        }, SPLASH);
    }
}