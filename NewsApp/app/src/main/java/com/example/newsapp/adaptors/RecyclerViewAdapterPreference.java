/*THE RECYCLER VIEW ADOPTER TO DEFINE VIEW*/
package com.example.newsapp.adaptors;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.example.newsapp.interfaces.OnPreferenceCheckChangeListener;
import com.example.newsapp.models.Preference;

import java.util.List;

public class RecyclerViewAdapterPreference extends RecyclerView.Adapter<RecyclerViewAdapterPreference.MyViewHolder> {

    List<Preference> preferences;
    Activity mAct;
    OnPreferenceCheckChangeListener onCheckChangeListener;

    public RecyclerViewAdapterPreference(List<Preference> preferences, Activity mAct, OnPreferenceCheckChangeListener onCheckChangeListener) {
        this.preferences = preferences;
        this.mAct = mAct;
        this.onCheckChangeListener = onCheckChangeListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference, parent, false);
        return new MyViewHolder(itemView, onCheckChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.data = preferences.get(position);
        holder.textView.setText(holder.data.getName());
    }


    @Override
    public int getItemCount() {
        return preferences.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        TextView textView;
        CheckBox checkBox;
        Preference data;
        OnPreferenceCheckChangeListener onCheckChangeListener;

        public MyViewHolder(@NonNull View itemView, OnPreferenceCheckChangeListener onCheckChangeListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            checkBox = itemView.findViewById(R.id.checkBox);
            this.onCheckChangeListener = onCheckChangeListener;
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            onCheckChangeListener.onPreferenceCheck(getAdapterPosition(), isChecked);
        }
    }
}