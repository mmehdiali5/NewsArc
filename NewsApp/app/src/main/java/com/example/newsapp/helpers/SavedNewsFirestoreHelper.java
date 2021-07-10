package com.example.newsapp.helpers;

import com.example.newsapp.interfaces.NewsDeleteListener;
import com.example.newsapp.interfaces.NewsSaveListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kwabenaberko.newsapilib.models.Article;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SavedNewsFirestoreHelper {

    public static void SaveNews(Article news, NewsSaveListener newsSaveListener) {

        String savedNewsId = news.getUrl().replace("/", "+");

        FirebaseFirestore.getInstance()
                .collection("userSavedNews")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo("savedNewsId", savedNewsId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().isEmpty()) {
                        newsSaveListener.onNewsSave(false);
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("savedNews")
                                .document(savedNewsId)
                                .set(news);

                        Map<String, String> map = new HashMap<>();

                        map.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        map.put("savedNewsId", savedNewsId);

                        FirebaseFirestore.getInstance()
                                .collection("userSavedNews")
                                .add(map)
                                .addOnCompleteListener(task1 -> {
                                    newsSaveListener.onNewsSave(true);
                                });
                    }
                });
    }

    static public void deleteSavedNews(String URL, NewsDeleteListener newsDeleteListener) {

        FirebaseFirestore.getInstance()
                .collection("userSavedNews")
                .whereEqualTo("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .whereEqualTo("savedNewsId", URL.replace("/", "+"))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseFirestore.getInstance()
                                .collection("userSavedNews")
                                .document(Objects.requireNonNull(task.getResult()).getDocuments().get(0).getId())
                                .delete()
                                .addOnCompleteListener(task1 -> {
                                    newsDeleteListener.onNewsDelete();
                                });
                    }
                });
    }
}