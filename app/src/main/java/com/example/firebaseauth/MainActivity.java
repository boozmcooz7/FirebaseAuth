package com.example.firebaseauth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button btnGoToLogin, btnGoToFilter, btnLogout, btnGoToRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //   ה-UI
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnGoToRecipes = findViewById(R.id.btnGoToRecipes);
        btnGoToFilter = findViewById(R.id.btnGoToFilter);
        btnLogout = findViewById(R.id.btnLogout);

        // הגדרת מאזינים ללחיצות על הכפתורים
        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        btnGoToRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(MainActivity.this, RecipeListActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "יש להתחבר קודם!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoToFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(MainActivity.this, FilteredRecipesActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "יש להתחבר קודם!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "התנתקת בהצלחה.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
