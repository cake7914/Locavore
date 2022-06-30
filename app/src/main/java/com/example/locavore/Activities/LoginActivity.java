package com.example.locavore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locavore.R;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etEmailAddress;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnFarmerSignup;
    private Button btnLocavoreSignup;
    private ImageView ivWaves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null)
        {
            goToMainActivity();
        }

        etEmailAddress = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        btnFarmerSignup = findViewById(R.id.btnFarmerSignup);
        btnFarmerSignup.setOnClickListener(v -> goToFarmerSignupActivity());

        btnLocavoreSignup = findViewById(R.id.btnLocavoreSignup);
        btnLocavoreSignup.setOnClickListener(v -> goToLocavoreSignupActivity());

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> loginUser(etEmailAddress.getText().toString(), etPassword.getText().toString()));

        ivWaves = findViewById(R.id.ivWaves);
        Glide.with(this)
                .load(getDrawable(R.drawable.login_screen))
                .centerCrop()
                .into(ivWaves);
    }
    private void goToFarmerSignupActivity() {
        Intent i = new Intent(this, FarmerSignupActivity.class);
        startActivity(i);
    }

    private void goToLocavoreSignupActivity() {
        Intent i = new Intent(this, LocavoreSignupActivity.class);
        startActivity(i);
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        //navigate to the main activity if the user has signed in properly.
        // Logging in in the background executes on the background thread rather than main or UI thread. Better for performance/ user experience
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if(e != null)
            {
                if(username.isEmpty())
                {
                    Log.e(TAG, "Username field empty", e);
                    Toast.makeText(LoginActivity.this, "Please enter a username to login", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()) {
                    Log.e(TAG, "Password field empty", e);
                    Toast.makeText(LoginActivity.this, "Please enter a password to login", Toast.LENGTH_SHORT).show();
                } else if(e.getCode() == 101) {
                    Log.e(TAG, "User does not exist", e);
                    Toast.makeText(LoginActivity.this, "Please enter a valid username and password to login.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Issue with login " + e.getCode(), e);
                    Toast.makeText(LoginActivity.this, "Issue with login!", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                goToMainActivity();
                Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
