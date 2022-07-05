package com.example.locavore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.locavore.AuthenticationManager;
import com.example.locavore.R;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etEmailAddress;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnFarmerSignup;
    private Button btnLocavoreSignup;
    private AuthenticationManager authenticationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        authenticationManager = new AuthenticationManager(this);

        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()) { // user is already logged in
            goToActivity(MainActivity.class);
            finish();
        }

        etEmailAddress = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        btnFarmerSignup = findViewById(R.id.btnFarmerSignup);
        btnFarmerSignup.setOnClickListener(v -> goToActivity(FarmerSignupActivity.class));

        btnLocavoreSignup = findViewById(R.id.btnLocavoreSignup);
        btnLocavoreSignup.setOnClickListener(v -> goToActivity(LocavoreSignupActivity.class));

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> authenticationManager.login(etEmailAddress.getText().toString(), etPassword.getText().toString()));
    }

    private void goToActivity(Class activity) {
        Intent i = new Intent(this, activity);
        startActivity(i);
    }
}
