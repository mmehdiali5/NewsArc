package com.example.newsapp.models;

import java.io.Serializable;
import java.util.List;

public class Preference implements Serializable {

    String name;
    List<String> keywords;

    public String getName() {
        return name;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}