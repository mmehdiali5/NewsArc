/*FOR SAVING THE NEWS*/
package com.example.newsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsapp.adaptors.RecyclerViewAdapter;
import com.example.newsapp.helpers.SavedNewsFirestoreHelper;
import com.example.newsapp.interfaces.AddItemListener;
import com.example.newsapp.interfaces.NewsDeleteListener;
import com.example.newsapp.interfaces.OnDeleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kwabenaberko.newsapilib.models.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SavedNews extends AppCompatActivity implements OnDeleteListener, AddItemListener, NewsDeleteListener {

    public static String SAVED_NEWS_TYPE = "SavedNews";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Article> savedNewsList;
    int savedNewsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        swipeRefreshLayout.setOnRefreshListener(() -> fetchSavedNews(SavedNews.this));

        savedNewsList = new ArrayList<>();
        savedNewsCount = 0;

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        fetchSavedNews(this::onAddItem);
    }

    public void fetchSavedNews(AddItemListener addItemListener) {

        savedNewsList.clear();

        FirebaseFirestore.getInstance()
                .collection("userSavedNews")
                .whereEqualTo("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            adapter = new RecyclerViewAdapter(SAVED_NEWS_TYPE, savedNewsList, SavedNews.this, null, SavedNews.this);
                            recyclerView.setAdapter(adapter);
                            swipeRefreshLayout.setRefreshing(false);
                            findViewById(R.id.textViewNoSavedNews).setVisibility(View.VISIBLE);
                        } else {
                            savedNewsCount = task.getResult().size();
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                FirebaseFirestore.getInstance()
                                        .collection("savedNews")
                                        .document(Objects.requireNonNull(document.getString("savedNewsId")))
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            savedNewsList.add(Objects.requireNonNull(task1.getResult()).toObject(Article.class));
                                            addItemListener.onAddItem();
                                        });
                            }
                        }
                    }
                });
    }

    @Override
    public void OnDeleteClick(int position) {
        swipeRefreshLayout.setRefreshing(true);
        SavedNewsFirestoreHelper.deleteSavedNews(savedNewsList.get(position).getUrl(), this::onNewsDelete);
    }

    @Override
    public void onAddItem() {
        if (savedNewsCount == savedNewsList.size()) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.textViewNoSavedNews).setVisibility(View.GONE);
            adapter = new RecyclerViewAdapter(SAVED_NEWS_TYPE, savedNewsList, SavedNews.this, null, SavedNews.this);
            recyclerView.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onNewsDelete() {
        Toast.makeText(getApplicationContext(), "News deleted successfully.", Toast.LENGTH_SHORT).show();
        fetchSavedNews(this::onAddItem);
    }
}