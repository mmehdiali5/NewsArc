/*TO DISPLAY DIFFERENT CATEGORIES TO THE USER*/
package com.example.newsapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsapp.adaptors.RecyclerViewAdapterCategory;
import com.example.newsapp.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewsCategories extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Category> newsCategories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        newsCategories = new ArrayList<>();
        fetchNewsCategories();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchNewsCategories();
        });
    }

    public void fetchNewsCategories() {

        newsCategories.clear();

        FirebaseFirestore.getInstance()
                .collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    int i = 0;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                            newsCategories.add(documentSnapshot.toObject(Category.class));
                        }

                        adapter = new RecyclerViewAdapterCategory(newsCategories, NewsCategories.this);
                        recyclerView.setAdapter(adapter);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}