package com.example.mytranslator.models;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@EqualsAndHashCode
public class Definition {
    public String dict;
    public String dictType;
    public Integer year;
    public ArrayList<String> text;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
