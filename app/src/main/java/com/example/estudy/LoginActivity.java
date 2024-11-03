package com.example.estudy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.N;

import java.time.LocalTime;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button button;
    private Button signupButton;
    private EditText userEditText, passwordEditText;
    private boolean isPasswordVisible = false;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    private TextView loginText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Log In Activity");
        passwordEditText = findViewById(R.id.password);
        userEditText = findViewById(R.id.username);
        button = findViewById(R.id.LoginButton);
        signupButton = findViewById(R.id.SignUpButton);
        progressBar = findViewById(R.id.progressbar); // Assuming you have a ProgressBar in your layout
        loginText = findViewById(R.id.loginText);
        passwordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalTime localTime = LocalTime.now();
            int hour = localTime.getHour();
            if(hour < 12 && hour >= 4){
                loginText.setText("Good Morning!");
            }
            else if(hour >= 12 && hour < 15){
                loginText.setText("Good Noon!");
            }
            else if(hour>=18 && hour < 22){
                loginText.setText("Good Evening!");
            }
            else if(hour < 18 && hour >= 15){
                loginText.setText("Good Afternoon!");
            }
            else loginText.setText("Go to Sleep!");
        }
        forgotPassword = findViewById(R.id.forgot_password);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, forgot_pass.class);
                startActivity(intent);
            });
        }
        button.setOnClickListener(v -> {
            if (validateUsername() && validatePassword()) {
                checkUser();
            } else {
                Toast.makeText(LoginActivity.this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            }
        });
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, registrationPage.class);
            startActivity(intent);
        });
    }
    private boolean validateUsername() {
        String val = userEditText.getText().toString().trim();
        if (val.isEmpty()) {
            userEditText.setError("Username/Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches() || !val.endsWith("@gmail.com")) {
            userEditText.setError("Enter a valid Gmail address");
            return false;
        }
        userEditText.setError(null);
        return true;
    }
    private boolean validatePassword() {
        String val = passwordEditText.getText().toString().trim();
        if (val.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return false;
        }
        passwordEditText.setError(null);
        return true;
    }
    private void checkUser() {
        String userUserName = userEditText.getText().toString().trim();
        String userPassword = passwordEditText.getText().toString().trim();
        String encodedUserName = encodeEmail(userUserName);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(encodedUserName).child("RegistrationPageInformation");
        //DatabaseReference usersData = FirebaseDatabase.getInstance().getReference("All");
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE); // Hide progress bar
                if (snapshot.exists()) {
                    String Name = snapshot.child("name").getValue(String.class);
//                    usersData.child(Name).child(encodeEmail(userUserName)).child("Name").setValue(Name);
//                    usersData.child(Name).child(encodeEmail(userUserName)).child("Email").setValue(userUserName);
                    String passwordFromDB = snapshot.child("password").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    if (Objects.equals(passwordFromDB, userPassword)) {
                        Toast.makeText(LoginActivity.this,"Wellcome "+ name, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomePage.class);
                        intent.putExtra("USER_EMAIL", userUserName);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    userEditText.setError("User does not exist");
                    userEditText.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide progress bar
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.visibility_off_24, 0); // Closed eye icon
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.visibility_24, 0); // Open eye icon
        }
        passwordEditText.setSelection(passwordEditText.length());
        isPasswordVisible = !isPasswordVisible;
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", null)
                .show();
    }
}