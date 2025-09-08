package com.example.firebaseauth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RecipeListActivity extends AppCompatActivity {
    private static final String TAG = "RecipeListActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etRecipeName, etIngredients, etServings;
    private Button btnAddRecipe;
    private RecyclerView rvRecipes;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // אתחול רכיבי ה-UI
        etRecipeName = findViewById(R.id.etRecipeName);
        etIngredients = findViewById(R.id.etIngredients);
        etServings = findViewById(R.id.etServings);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        rvRecipes = findViewById(R.id.rvRecipes);

        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(recipeList);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        // הגדרת מאזין ללחיצה על כפתור הוספת המתכון
        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecipe();
            }
        });

        fetchRecipes();
    }

    private void addRecipe() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "יש להתחבר כדי להוסיף מתכון", Toast.LENGTH_SHORT).show();
            return;
        }

        String recipeName = etRecipeName.getText().toString();
        String ingredients = etIngredients.getText().toString();
        int servings = 0;
        try {
            servings = Integer.parseInt(etServings.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "כמות המנות חייבת להיות מספר", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת אובייקט מתכון
        Recipe newRecipe = new Recipe(recipeName, ingredients, servings);

        // שמירת המתכון ב-Firestore
        db.collection("users").document(user.getUid())
                .collection("recipes")
                .add(newRecipe)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(this, "המתכון נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    etRecipeName.setText("");
                    etIngredients.setText("");
                    etServings.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(this, "שגיאה בשמירת המתכון", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchRecipes() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        CollectionReference recipesRef = db.collection("users").document(user.getUid()).collection("recipes");
        firestoreListener = recipesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                recipeList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Recipe recipe = doc.toObject(Recipe.class);
                    recipeList.add(recipe);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    // קלאס נתונים עבור מתכון
    public static class Recipe {
        private String name;
        private String ingredients;
        private int servings;

        public Recipe() {
            // נדרש ע"י Firestore
        }

        public Recipe(String name, String ingredients, int servings) {
            this.name = name;
            this.ingredients = ingredients;
            this.servings = servings;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        public int getServings() { return servings; }
        public void setServings(int servings) { this.servings = servings; }
    }

    // מתאם (Adapter) עבור ה-RecyclerView
    public static class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
        private List<Recipe> recipes;

        public RecipeAdapter(List<Recipe> recipes) {
            this.recipes = recipes;
        }

        @Override
        public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecipeViewHolder holder, int position) {
            Recipe recipe = recipes.get(position);
            holder.text1.setText("שם: " + recipe.getName());
            holder.text2.setText("מנות: " + recipe.getServings());
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        static class RecipeViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            RecipeViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
