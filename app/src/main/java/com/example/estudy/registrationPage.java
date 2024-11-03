package com.example.estudy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class registrationPage extends AppCompatActivity implements View.OnClickListener {
    private EditText RegisternameEditText, RegisteremailEditText, RegisterphoneEditText, RegisterPasswordEditText;
    private Button RegisterButton;
    private boolean isPasswordVisible = false;
    private FirebaseAuth nAuth;
    private ProgressBar progressBar;
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("^[+][0-9]{10,15}$");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);
        this.setTitle("Registration Activity");

        nAuth = FirebaseAuth.getInstance();
        RegisternameEditText = findViewById(R.id.name);
        RegisteremailEditText = findViewById(R.id.email);
        RegisterphoneEditText = findViewById(R.id.PhoneNumber);
        RegisterPasswordEditText = findViewById(R.id.Password_register);
        progressBar = findViewById(R.id.progressbarId);
        RegisterButton = findViewById(R.id.RegisterButton);
        RegisterButton.setOnClickListener(this);

        RegisterPasswordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (RegisterPasswordEditText.getRight() - RegisterPasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.RegisterButton) {
            userRegister();
        }
    }

    private void userRegister() {
        String name = RegisternameEditText.getText().toString().trim();
        String email = RegisteremailEditText.getText().toString().trim();
        String phone = RegisterphoneEditText.getText().toString().trim();
        String password = RegisterPasswordEditText.getText().toString().trim();

        // Input validations
        if (name.isEmpty()) {
            RegisternameEditText.setError("Enter your name");
            RegisternameEditText.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            RegisteremailEditText.setError("Enter an email address");
            RegisteremailEditText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
            RegisteremailEditText.setError("Enter a valid Gmail address");
            RegisteremailEditText.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            RegisterphoneEditText.setError("Enter your phone number");
            RegisterphoneEditText.requestFocus();
            return;
        }
//        if (!INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches()) {
//            RegisterphoneEditText.setError("Enter a valid international phone number (e.g., +1234567890)");
//            RegisterphoneEditText.requestFocus();
//            return;
//        }
        if (password.isEmpty()) {
            RegisterPasswordEditText.setError("Enter a password");
            RegisterPasswordEditText.requestFocus();
            return;
        }
        if (password.length() < 6) {
            RegisterPasswordEditText.setError("Password must be at least 6 characters");
            RegisterPasswordEditText.requestFocus();
            return;
        }
//        else {
//            boolean boro = false, Chuto = false, digit = false, special = false;
//            //Toast.makeText(registrationPage.this, password, Toast.LENGTH_LONG).show();
//            for(int i = 0; i < password.length(); i++){
//                if(password.charAt(i) >= 'A' && password.charAt(i) <= 'Z'){
//                    boro = true;
//                }
//                if(password.charAt(i) >= 'a' && password.charAt(i) <= 'z'){
//                    Chuto = true;
//                }
//                if(password.charAt(i) >= '0' && password.charAt(i) <= '9'){
//                    digit = true;
//                }
//                if(password.charAt(i) == '@' || password.charAt(i) == '#' || password.charAt(i) == '$' || password.charAt(i) == '%' || password.charAt(i) == '^' || password.charAt(i) == '&') {
//                    special = true;
//                }
//            }
//            if(boro==false || Chuto==false || digit==false || special==false){
//                RegisterPasswordEditText.setError("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
//                RegisterPasswordEditText.requestFocus();
//                return;
//            }
//        }
        createInformationTable(email);
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        // Register the user with Firebase Authentication
        nAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, save user info
                            FirebaseUser firebaseUser = nAuth.getCurrentUser();
                            String profilePictureUrl = "https://your-storage-url/profile.jpg";
                            String coverPhotoUrl = "https://your-storage-url/cover.jpg";
                            saveUserInfo(firebaseUser.getEmail(), password, name, phone, profilePictureUrl, coverPhotoUrl);
                        } else {
                            Toast.makeText(registrationPage.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide progress bar
                        }
                    }
                });
    }
    private void saveUserInfo(String email, String password, String name, String phone, String profilePictureUrl, String coverPhotoUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        DatabaseReference usersData = FirebaseDatabase.getInstance().getReference("SearchALL");

        user usr = new user(
                name, password, phone, email, profilePictureUrl, coverPhotoUrl);
        usersData.child(name).child(encodeEmail(email)).child("Name").setValue(name);
        usersData.child(name).child(encodeEmail(email)).child("Email").setValue(email);
        myRef.child(encodeEmail(email)).child("RegistrationPageInformation").setValue(usr).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE); // Hide progress bar after save attempt
                if (task.isSuccessful()) {
                    Toast.makeText(registrationPage.this, "User information saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(registrationPage.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(registrationPage.this,registrationPage.class);
                    Toast.makeText(registrationPage.this, "Failed to save user information: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            RegisterPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            RegisterPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.visibility_off_24, 0); // Closed eye icon
        } else {
            RegisterPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            RegisterPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.visibility_24, 0); // Open eye icon
        }
        RegisterPasswordEditText.setSelection(RegisterPasswordEditText.length());
        isPasswordVisible = !isPasswordVisible;
    }
    private void createInformationTable(String userEmail){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.child(encodeEmail(userEmail)).child("Information").child("about_me").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("education").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("hometown").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("social_links").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("workplace").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("skills").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("profilePictureUrl").setValue("");
        myRef.child(encodeEmail(userEmail)).child("Information").child("coverPhotoUrl").setValue("");
    }
}
