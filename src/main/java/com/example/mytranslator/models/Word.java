package com.example.mytranslator.models;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Word {
    public String word;
    public String word_en;
    public String type;
    public ArrayList<String> singular;
    public ArrayList<String> plural;
    public Set<Definition> definitions;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
