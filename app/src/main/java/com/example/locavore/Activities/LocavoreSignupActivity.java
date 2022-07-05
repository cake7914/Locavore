package com.example.locavore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.AuthenticationManager;

public class LocavoreSignupActivity extends AppCompatActivity {

    public static final String TAG = "LocavoreSignupActivity";
    private Button btnSignup;
    private EditText etName;
    private EditText etEmailAddress;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etRadius;
    private AuthenticationManager authenticationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locavore_signup);
        authenticationManager = new AuthenticationManager(this);

        etName = findViewById(R.id.etName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etRadius = findViewById(R.id.etRadius);

        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String emailAddress = etEmailAddress.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            int radius = Integer.parseInt(etRadius.getText().toString());
            authenticationManager.signup(User.CONSUMER_USER_TYPE, name, emailAddress, username, password, "", "", radius);
        });
    }
}