package com.example.newsapp.helpers;

import com.example.newsapp.models.Preference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PreferencesFirestoreHelper {

    public static void addUserPreferences(List<Preference> preferences) {

        deleteExistingUserPreferences();
        addUserPreferencesHelper(preferences);
    }

    static void addUserPreferencesHelper(List<Preference> preferences) {

        for (int i = 0; i < preferences.size(); ++i) {
            FirebaseFirestore.getInstance()
                    .collection("preferences")
                    .whereEqualTo("name", preferences.get(i).getName())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Map<String, String> map = new HashMap<>();
                            map.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            map.put("preferenceId", Objects.requireNonNull(task.getResult()).getDocuments().get(0).getId());
                            FirebaseFirestore.getInstance()
                                    .collection("userPreferences")
                                    .add(map);
                        }
                    });
        }
    }

    public static void deleteExistingUserPreferences() {

        FirebaseFirestore.getInstance()
                .collection("userPreferences")
                .whereEqualTo("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                            FirebaseFirestore.getInstance()
                                    .collection("userPreferences")
                                    .document(documentSnapshot.getId())
                                    .delete();
                        }
                    }
                });
    }
}