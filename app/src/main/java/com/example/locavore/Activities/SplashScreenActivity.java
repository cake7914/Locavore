package com.example.locavore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.locavore.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class SplashScreenActivity extends Activity {
    Handler handler;
    Context context;
    ImageView ivBlur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ivBlur = findViewById(R.id.ivBlur);
        Glide.with(this)
                .load(getDrawable(R.drawable.splash_screen))
                .centerCrop()
                .into(ivBlur);

        handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent=new Intent(SplashScreenActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        },2000);

    }

}