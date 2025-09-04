package com.example.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private TextView goToSignInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // --- 1. Initialize Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();

        // --- 2. Initialize UI Views ---
        emailEditText = findViewById(R.id.signUpEmailEditText);
        passwordEditText = findViewById(R.id.signUpPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        goToSignInTextView = findViewById(R.id.goToSignInTextView);

        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                createAccount(email, password);
            } else {
                Toast.makeText(SignUpActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            }
        });
        goToSignInTextView.setOnClickListener(v -> {
            // Finish this activity and go back to the sign-in screen
            finish();
        });
    }
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Log.d(TAG, "createUserWithEmail:success");
                        Toast.makeText(SignUpActivity.this, "Account created successfully.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }



    }


