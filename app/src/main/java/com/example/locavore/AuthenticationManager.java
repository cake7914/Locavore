package com.example.locavore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.locavore.Activities.MainActivity;
import com.example.locavore.Activities.SplashScreenActivity;
import com.example.locavore.Models.User;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Objects;

public class AuthenticationManager {

    Context context;
    public static final String TAG = "AuthenticationManager";
    public static final String STR_LOGIN = "login";
    public static final String STR_SIGNUP = "signup";
    private static final double METERS_TO_MILE = 1609.34;
    public static final int USER_NONEXISTENT_ERROR = 101;
    public static final int USER_ALREADY_EXISTS_ERROR = 202;
    public static final int MIN_PASSWORD_LENGTH = 10;
    private DataManager dataManager = DataManager.getInstance(null);

    public AuthenticationManager(Context context) {
        this.context = context;
    }

    public boolean validateFields(String operation, String userType, String name, String emailAddress, String username, String password, String bio, String address, int radius) {
        if (Objects.equals(operation, STR_SIGNUP) && password.length() < MIN_PASSWORD_LENGTH) { // check password length (and complexity?)
            Toast.makeText(context, context.getString(R.string.short_password), Toast.LENGTH_SHORT).show();
            return false;
        }  else if(name.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
        } else if(emailAddress.isEmpty()) { // check email is a valid email?
            Toast.makeText(context, context.getString(R.string.empty_email), Toast.LENGTH_SHORT).show();
        } else if(bio.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.empty_bio), Toast.LENGTH_SHORT).show();
        } else if(address.isEmpty()) { // check if address is a valid address?
            Toast.makeText(context, context.getString(R.string.empty_address), Toast.LENGTH_SHORT).show();
        } else if(radius < 5 || radius > 25) {
            Toast.makeText(context, context.getString(R.string.invalid_radius), Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public boolean checkParseErrors(ParseException e, String operation, String userType, String name, String emailAddress, String username, String password, String bio, String address, int radius) {
        if (e != null) { // a Parse error has occurred
            if (username.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.username_field_empty), Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.password_field_empty), Toast.LENGTH_SHORT).show();
            } else {
                if (Objects.equals(operation, STR_LOGIN)) {
                    if (e.getCode() == USER_NONEXISTENT_ERROR) {
                        Toast.makeText(context, context.getString(R.string.invalid_user), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Issue with login " + e.getCode(), e);
                        Toast.makeText(context, context.getString(R.string.misc_login_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (e.getCode() == USER_ALREADY_EXISTS_ERROR) {
                        Toast.makeText(context, context.getString(R.string.user_already_exists), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Issue with signup " + e.getCode(), e);
                        Toast.makeText(context, context.getString(R.string.misc_signup_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            return false;
        }
        return true;
    }

    public void signup(String userType, String name, String emailAddress, String username, String password, String bio, String address, int radius) {
        ParseUser user = new ParseUser();
        user.put(User.KEY_NAME, name);
        user.setEmail(emailAddress);
        user.setUsername(username);
        user.setPassword(password);
        user.put(User.KEY_BIO, bio);
        user.put(User.KEY_ADDRESS, address);
        user.put(User.KEY_USER_TYPE, userType);
        user.put(User.KEY_RADIUS, radius * METERS_TO_MILE);
        if(validateFields(STR_SIGNUP, userType, name, emailAddress, username, password, bio, address, radius)) {
            user.signUpInBackground(e -> {
                if (checkParseErrors(e, STR_SIGNUP, userType, name, emailAddress, username, password, bio, address, radius)) {
                    dataManager.mRadius = radius;
                    goToSplashScreenActivity((Activity) context);
                    Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void login(String username, String password) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if(checkParseErrors(e, STR_LOGIN, "", "", "", username, password, "", "", 0))
            {
                dataManager.mRadius = ParseUser.getCurrentUser().getInt(User.KEY_RADIUS);
                goToSplashScreenActivity((Activity) context);
                Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToSplashScreenActivity(Activity activity) {
        Intent i = new Intent(activity, SplashScreenActivity.class);
        activity.startActivity(i);
        activity.finish();
    }
}
