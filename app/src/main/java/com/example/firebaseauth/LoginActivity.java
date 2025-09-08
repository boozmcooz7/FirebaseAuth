package com.example.firebaseauth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private EditText etEmail, etPassword;
    private Button btnRegister, btnLogin;
    private ImageView ivProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //  Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //   UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        // הגדרת מאזין ללחיצה על כפתור ההרשמה
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // הגדרת מאזין ללחיצה על כפתור ההתחברות
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }
    public void chooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "בחר תמונה"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
            Toast.makeText(this, "תמונה נבחרה בהצלחה", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create a document in Firestore for the new user
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", user.getEmail());
                            userData.put("createdAt", System.currentTimeMillis());
                            userData.put("isPremiumUser", false); // Example for another personal data point

                            if (imageUri != null) {
                                uploadImage(user, userData);
                            } else {
                                saveUserData(user, userData);
                            }
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "הרשמה נכשלה. נסה שוב.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage(FirebaseUser user, Map<String, Object> userData) {
        if (imageUri != null) {
            StorageReference fileRef = storage.getReference("uploads/" + user.getUid());

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            userData.put("profileImageUrl", uri.toString());
                            saveUserData(user, userData);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "שגיאה בהעלאת התמונה.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Image upload failed", e);
                        saveUserData(user, userData);
                    });
        }
    }

    private void saveUserData(FirebaseUser user, Map<String, Object> userData) {
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "הרשמה מוצלחת ופרטי משתמש נשמרו.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, RecipeListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding user data", e);
                    Toast.makeText(LoginActivity.this, "שגיאה בשמירת פרטי המשתמש.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(LoginActivity.this, "התחברת בהצלחה.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, RecipeListActivity.class));
                        finish();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "התחברות נכשלה. בדוק את הפרטים.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
