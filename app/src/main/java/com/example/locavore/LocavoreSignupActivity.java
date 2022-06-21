package com.example.locavore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;

public class LocavoreSignupActivity extends AppCompatActivity {

    public static final String TAG = "LocavoreSignupActivity";
    private Button btnSignup;
    private EditText etName;
    private EditText etEmailAddress;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locavore_signup);

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
            String radius = etRadius.getText().toString();

            signupLocavore(name, emailAddress, username, password, radius);
        });
    }

    private void signupLocavore(String name, String emailAddress, String username, String password, String radius) {
        Log.i(TAG, "Attempting to signup new locavore user");
        ParseUser user = new ParseUser();

        user.put(MyUser.KEY_NAME, name);
        user.setEmail(emailAddress);
        user.setUsername(username);
        user.setPassword(password);
        user.put(MyUser.KEY_USER_TYPE, "locavore");
        user.put(MyUser.KEY_RADIUS, radius);
        //user.put(MyUser.KEY_PUSH_NOTIFS_ENABLED, )

        user.signUpInBackground(e -> {
            if (e != null) {
                if(username.isEmpty()) /* TODO: add more error handling for other fields*/
                {
                    Log.e(TAG, "Username field empty", e);
                    Toast.makeText(LocavoreSignupActivity.this, "Please enter a username to signup", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()) {
                    Log.e(TAG, "Password field empty", e);
                    Toast.makeText(LocavoreSignupActivity.this, "Please enter a password to signup", Toast.LENGTH_SHORT).show();
                } else if(e.getCode() == 202) {
                    Log.e(TAG, "User already exists", e);
                    Toast.makeText(LocavoreSignupActivity.this, "This user already exists; Please try a new username!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Issue with signup" + e.getCode(), e);
                    Toast.makeText(LocavoreSignupActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                }
            } else {
                goToMainActivity();
                Toast.makeText(LocavoreSignupActivity.this, "Signup Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}