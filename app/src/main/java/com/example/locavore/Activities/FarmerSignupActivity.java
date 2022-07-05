package com.example.locavore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.locavore.Models.User;
import com.example.locavore.R;
import com.example.locavore.AuthenticationManager;

public class FarmerSignupActivity extends AppCompatActivity {

    public static final String TAG = "FarmerSignupActivity";
    private Button btnSignup;
    private EditText etFarmName;
    private EditText etEmailAddress;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etBio;
    private EditText etAddress;
    private AuthenticationManager authenticationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_signup);
        authenticationManager = new AuthenticationManager(this);

        etFarmName = findViewById(R.id.etName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etBio = findViewById(R.id.etBio);
        etAddress = findViewById(R.id.etLocation);
        btnSignup = findViewById(R.id.btnFarmerSignup);

        btnSignup.setOnClickListener(v -> {
            String farmName = etFarmName.getText().toString();
            String emailAddress = etEmailAddress.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String bio = etBio.getText().toString();
            String address = etAddress.getText().toString();
            // add tags as well

            authenticationManager.signup(User.FARM_USER_TYPE, farmName, emailAddress, username, password, bio, address, 0);
        });
    }
}