/*TO ADD PREFERENCES FOR THE USER*/
package com.example.newsapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.adaptors.RecyclerViewAdapterPreference;
import com.example.newsapp.helpers.PreferencesFirestoreHelper;
import com.example.newsapp.interfaces.OnPreferenceCheckChangeListener;
import com.example.newsapp.models.Preference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Preferences extends AppCompatActivity implements OnPreferenceCheckChangeListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapterPreference adapter;
    List<Preference> preferences;
    List<Preference> userPreferences;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        fetchPreferences();
        userPreferences = new ArrayList<>();
    }

    public void fetchPreferences() {

        preferences = new ArrayList<>();

        FirebaseFirestore.getInstance()
                .collection("preferences")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            preferences.add(documentSnapshot.toObject(Preference.class));
                        }
                        findViewById(R.id.addPreferences).setVisibility(View.VISIBLE);
                        adapter = new RecyclerViewAdapterPreference(preferences, Preferences.this, Preferences.this);
                        recyclerView.setAdapter(adapter);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                });

    }

    @Override
    public void onPreferenceCheck(int position, boolean isChecked) {
        if (isChecked) {
            userPreferences.add(preferences.get(position));
        } else {
            userPreferences.remove(preferences.get(position));
        }
    }

    public void updateUserPreferences(View view) {
        if (userPreferences.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No preferences selected.", Toast.LENGTH_SHORT).show();
        } else {
            PreferencesFirestoreHelper.addUserPreferences(userPreferences);
            Intent intent = new Intent(getApplicationContext(), Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("userPreferences", (Serializable) userPreferences);
            startActivity(intent);
            finish();
        }
    }
}