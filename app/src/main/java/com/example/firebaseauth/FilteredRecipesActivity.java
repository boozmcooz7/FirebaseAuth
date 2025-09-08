package com.example.firebaseauth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

// שימוש חוזר בקלאס Recipe ו-RecipeAdapter מקובץ RecipeListActivity.java
// אתה יכול להעתיק אותם לכאן או להפוך אותם לקלאסים ציבוריים בפרויקט שלך.

public class FilteredRecipesActivity extends AppCompatActivity {
    private static final String TAG = "FilteredRecipesActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etMinServings;
    private Button btnApplyFilter;
    private RecyclerView rvFilteredRecipes;
    private RecipeListActivity.RecipeAdapter adapter;
    private List<RecipeListActivity.Recipe> filteredRecipeList;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_recipes);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // אתחול רכיבי ה-UI
        etMinServings = findViewById(R.id.etMinServings);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        rvFilteredRecipes = findViewById(R.id.rvFilteredRecipes);

        filteredRecipeList = new ArrayList<>();
        adapter = new RecipeListActivity.RecipeAdapter(filteredRecipeList);
        rvFilteredRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvFilteredRecipes.setAdapter(adapter);

        // הגדרת מאזין לכפתור הסינון
        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        int minServings = 0;
        try {
            minServings = Integer.parseInt(etMinServings.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "יש להזין מספר", Toast.LENGTH_SHORT).show();
            return;
        }

        // אם קיים מאזין קודם, יש להסירו
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        // בניית השאילתה עם תנאי הסינון
        CollectionReference recipesRef = db.collection("users").document(user.getUid()).collection("recipes");
        Query filteredQuery = recipesRef.whereGreaterThanOrEqualTo("servings", minServings);

        // הפעלת מאזין חדש על השאילתה המסוננת
        firestoreListener = filteredQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                filteredRecipeList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    RecipeListActivity.Recipe recipe = doc.toObject(RecipeListActivity.Recipe.class);
                    filteredRecipeList.add(recipe);
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(FilteredRecipesActivity.this, "הסינון בוצע בהצלחה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
