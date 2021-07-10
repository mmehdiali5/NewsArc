/*TO DISPLAY THE NEWS AT HOMEPAGE TO THE USER*/
package com.example.newsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsapp.adaptors.RecyclerViewAdapter;
import com.example.newsapp.firebaseauth.SignIn;
import com.example.newsapp.helpers.SavedNewsFirestoreHelper;
import com.example.newsapp.interfaces.AddItemListener;
import com.example.newsapp.interfaces.NewsSaveListener;
import com.example.newsapp.interfaces.OnSaveListener;
import com.example.newsapp.models.Category;
import com.example.newsapp.models.Preference;
import com.google.android.material.navigation.NavigationView;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Home extends AppCompatActivity implements OnSaveListener, AddItemListener, NewsSaveListener {

    String DEFAULT_QUERY = "politics OR health OR sports OR technology OR crypto OR entertainment OR industry";
    String DEFAULT_SORT_BY = "popularity";
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Article> newsList;
    List<Preference> userPreferences;
    int userPreferenceCount = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SimpleDateFormat", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FileProvider fileProvider;

        if (getIntent().getExtras() != null) {
            userPreferences = (List<Preference>) getIntent().getSerializableExtra("userPreferences");
        } else {
            userPreferences = new ArrayList<>();
        }
        userPreferenceCount = userPreferences.size();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            findViewById(R.id.account).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        fetchNewsFromAPI();

        swipeRefreshLayout.setOnRefreshListener(this::fetchNewsFromAPI);

        navigationView.setNavigationItemSelectedListener(this::chooseItem);

        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!Strings.isNullOrEmpty(query)) {
                    Intent intent = new Intent(getApplicationContext(), CategoryNews.class);
                    Category category = new Category();
                    category.setName(query);
                    String[] keywords = {query};
                    category.setKeywords(Arrays.asList(keywords));
                    intent.putExtra("category", category);
                    startActivity(intent);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void fetchNewsFromAPI() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!userPreferences.isEmpty()) {
                fetchNewsHelper(getUserPreferencesQuery(), "relevancy");
            } else {
                fetchUserPreferences(Home.this);
            }
        } else
            fetchNewsHelper(DEFAULT_QUERY, DEFAULT_SORT_BY);
    }

    String getUserPreferencesQuery() {

        if (userPreferences.isEmpty())
            return DEFAULT_QUERY;
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < userPreferences.size(); ++i)
            query.append(userPreferences.get(i).getName()).append(" OR ");
        for (int i = 0; i < userPreferences.size(); ++i) {
            List<String> keywords = userPreferences.get(i).getKeywords();
            for (int j = 0; j < keywords.size(); ++j) {
                query.append(keywords.get(j)).append(" OR ");
            }
        }
        return query.toString().substring(0, query.toString().length() - 4);
    }

    @Override
    public void onAddItem() {
        if (userPreferences.size() == userPreferenceCount) {
            fetchNewsHelper(getUserPreferencesQuery(), "relevancy");
        }
    }

    public void fetchUserPreferences(AddItemListener addItemListener) {

        userPreferences.clear();
        FirebaseFirestore.getInstance()
                .collection("userPreferences")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        userPreferenceCount = Objects.requireNonNull(task.getResult()).size();
                        if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                            fetchNewsHelper(DEFAULT_QUERY, DEFAULT_SORT_BY);
                        } else {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                FirebaseFirestore.getInstance()
                                        .collection("preferences")
                                        .document(Objects.requireNonNull(documentSnapshot.getString("preferenceId")))
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            userPreferences.add(task1.getResult().toObject(Preference.class));
                                            addItemListener.onAddItem();
                                        });
                            }
                        }
                    }
                });
    }

    public void fetchNewsHelper(String query, String sortBy) {

        String API_KEY = "fe0861a443724c28997a1fbe88d2e590";
        NewsApiClient newsApiClient = new NewsApiClient(API_KEY);
        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .q(query)
                        .language("en")
                        .sortBy(sortBy)
                        .pageSize(100)
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        newsList = response.getArticles();

                        int i = 0;
                        while (i < newsList.size()) {
                            if (Strings.isNullOrEmpty(newsList.get(i).getUrl())) {
                                newsList.remove(i);
                            } else {
                                i++;
                            }
                        }

                        adapter = new RecyclerViewAdapter("News", newsList, Home.this, Home.this, null);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        recyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    boolean chooseItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                moveToPreferences();
                break;
            case R.id.saved:
                savedNews();
                break;
            case R.id.aboutUs:
                moveToAboutUs();
                break;
            case R.id.signOut:
                logout();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer((GravityCompat.END));
        } else {
            super.onBackPressed();
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), SignIn.class));
        finish();
    }

    public void savedNews() {

        if (FirebaseAuth.getInstance().getUid() == null)
            startActivity(new Intent(getApplicationContext(), SignIn.class));

        else
            startActivity(new Intent(getApplicationContext(), SavedNews.class));

    }

    public void moveToAboutUs() {
        startActivity(new Intent(getApplicationContext(), AboutUs.class));
    }

    public void moveToPreferences() {
        startActivity(new Intent(getApplicationContext(), Preferences.class));
    }

    @Override
    public void OnSaveClick(int position) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), SignIn.class));
        } else {
            SavedNewsFirestoreHelper.SaveNews(newsList.get(position), this::onNewsSave);
        }
    }

    public void headlines(View view) {

        Intent intent = new Intent(getApplicationContext(), CategoryNews.class);
        Category category = new Category();
        category.setName("Headlines");
        String[] keywords = {"headlines", "politics", "health", "sports", "climate", "technology", "crypto", "entertainment", "industry"};
        category.setKeywords(Arrays.asList(keywords));
        intent.putExtra("category", category);
        startActivity(intent);
    }

    public void moveToNewsCategories(View view) {
        Intent intent = new Intent(getApplicationContext(), NewsCategories.class);
        startActivity(intent);
    }

    @SuppressLint("WrongConstant")
    public void openNavigationDrawer(View view) {
        drawerLayout.openDrawer(Gravity.END);
    }

    @Override
    public void onNewsSave(boolean saveStatus) {
        Toast.makeText(this, saveStatus ? "New saved successfully." : "News saved already.", Toast.LENGTH_SHORT).show();
    }
}