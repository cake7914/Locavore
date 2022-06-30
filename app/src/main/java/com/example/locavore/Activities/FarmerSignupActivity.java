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
import com.example.locavore.Models.Farm;
import com.example.locavore.R;
import com.parse.ParseUser;

public class FarmerSignupActivity extends AppCompatActivity {

    public static final String TAG = "FarmerSignupActivity";
    private Button btnSignup;
    private EditText etFarmName;
    private EditText etEmailAddress;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etBio;
    private EditText etAddress;
    private ImageView ivWaves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_signup);

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

            signupFarmer(farmName, emailAddress, username, password, bio, address);
        });

        ivWaves = findViewById(R.id.ivSignUpWaves);
        Glide.with(this)
                .load(getDrawable(R.drawable.inverse_login_screen2))
                .centerCrop()
                .into(ivWaves);

    }

    private void signupFarmer(String farmName, String emailAddress, String username, String password, String bio, String address) {
        Log.i(TAG, "Attempting to signup new farmer");
        ParseUser user = new ParseUser();
        user.put(Farm.KEY_NAME, farmName);
        user.setEmail(emailAddress);
        user.setUsername(username);
        user.setPassword(password);
        user.put(Farm.KEY_BIO, bio);
        user.put(Farm.KEY_ADDRESS, address);
        user.put(Farm.KEY_USER_TYPE, "farmer");

        user.signUpInBackground(e -> {
            if (e != null) {
                if(username.isEmpty()) /* TODO: add more error handling for other fields*/
                {
                    Log.e(TAG, "Username field empty", e);
                    Toast.makeText(FarmerSignupActivity.this, "Please enter a username to signup", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()) {
                    Log.e(TAG, "Password field empty", e);
                    Toast.makeText(FarmerSignupActivity.this, "Please enter a password to signup", Toast.LENGTH_SHORT).show();
                } else if(e.getCode() == 202) {
                    Log.e(TAG, "User already exists", e);
                    Toast.makeText(FarmerSignupActivity.this, "This user already exists; Please try a new username!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Issue with signup" + e.getCode(), e);
                    Toast.makeText(FarmerSignupActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                }
            } else {
                goToMainActivity();
                Toast.makeText(FarmerSignupActivity.this, "Signup Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


}