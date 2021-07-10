/*VIEW HOLDER CLASS FOR CATEGORY*/
package com.example.newsapp.adaptors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.CategoryNews;
import com.example.newsapp.R;
import com.example.newsapp.models.Category;

import java.util.List;

public class RecyclerViewAdapterCategory extends RecyclerView.Adapter<RecyclerViewAdapterCategory.MyViewHolder> {

    List<Category> newsCategories;
    Activity mAct;

    public RecyclerViewAdapterCategory(List<Category> newsCategories, Activity activity) {
        this.newsCategories = newsCategories;
        this.mAct = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(newsCategories.get(position).getName().substring(0, 1).toUpperCase() + newsCategories.get(position).getName().substring(1));
        holder.imageView.setImageResource(mAct.getResources().getIdentifier("img_" + newsCategories.get(position).getName(), "drawable", mAct.getPackageName()));
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(mAct.getApplicationContext(), CategoryNews.class);
            intent.putExtra("category", newsCategories.get(position));
            mAct.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsCategories.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView textView;
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textView = itemView.findViewById(R.id.textView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}