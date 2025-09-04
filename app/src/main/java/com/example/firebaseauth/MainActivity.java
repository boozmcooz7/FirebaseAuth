package com.example.firebaseauth; // Make sure this matches your package name

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI elements
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private SignInButton googleSignInButton;
    private TextView statusTextView;

    // Firebase
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView goToSignUpTextView;


    // Activity Result Launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. Initialize Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();

        // --- 2. Initialize UI Views ---
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        statusTextView = findViewById(R.id.statusTextView);
        goToSignUpTextView = findViewById(R.id.goToSignUpTextView);


        // --- 3. Configure Google Sign-In ---
        // We configure Google Sign-In to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.project_id)) // Get this from your google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // --- 4. Set up the Google Sign-In Launcher ---
        // This replaces the deprecated onActivityResult
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        // --- 5. Set up Button Click Listeners ---
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                signInWithEmailPassword(email, password);
            } else {
                Toast.makeText(MainActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            }
        });
        goToSignUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Handles the Email/Password sign-in process.
     */
    private void signInWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    /**
     * Starts the Google Sign-In intent.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Authenticates with Firebase using the Google account token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Google Sign-In successful.", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }


    /**
     * Updates the UI based on the current user's sign-in state.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            statusTextView.setText("Signed In: " + user.getEmail());
            emailEditText.setVisibility(View.GONE);
            passwordEditText.setVisibility(View.GONE);
            signInButton.setText("Sign Out");
            signInButton.setOnClickListener(v -> signOut());
            googleSignInButton.setVisibility(View.GONE);
        } else {
            // User is signed out
            statusTextView.setText("Signed Out");
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            signInButton.setText("Sign In");
            signInButton.setOnClickListener(v -> {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signInWithEmailPassword(email, password);
            });
            googleSignInButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Signs out the current user from Firebase and Google.
     */
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    updateUI(null);
                    Toast.makeText(MainActivity.this, "Signed out.", Toast.LENGTH_SHORT).show();
                });
    }
}
