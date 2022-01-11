package com.example.mytranslator.repositories;

import com.example.mytranslator.models.Definition;
import com.example.mytranslator.models.Word;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordTranslatorRepository {
    private Gson gson = new Gson();
    public String translateWord(String word, String language){
        String fileName = "src/main/resources/translations/" +  language + "/"  + word + ".json";
        try {
            Reader reader = Files.newBufferedReader(Paths.get(fileName));
            Word wordModel = gson.fromJson(reader, Word.class);
            reader.close();
            return wordModel.toString();
        } catch (Exception e) {
            return "word not found";
        }
    }

    public boolean addWord(Word word, String language){
        String fileName = "src/main/resources/translations/" +  language + "/"  + word.word + ".json";
        try {
            Writer writer = new FileWriter(fileName);
            gson.toJson(word, writer);
            writer.close();
        } catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean deleteWord(String word, String language){
        String fileName = "src/main/resources/translations/" +  language + "/"  + word + ".json";
        try {
            File file = new File(fileName);
            file.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addDefinitionForWord(String word, String language, Definition definition){
        String fileName = "src/main/resources/translations/" +  language + "/"  + word + ".json";
        try {
            Reader reader = Files.newBufferedReader(Paths.get(fileName));
            Word wordModel = gson.fromJson(reader, Word.class);
            reader.close();
            if(!wordModel.definitions.add(definition)) return false;//todo
            try {
                Writer writer = new FileWriter(fileName);
                gson.toJson(wordModel, writer);
                writer.close();
            } catch (Exception e){
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean removeDefinition(String word, String language, String dictionary) {
        Word wordModel = readWordModel(getFilePath(word, language), language);
        Optional<Definition> definition = wordModel.definitions.stream()
                .filter(def -> def.dict.equals(dictionary))
                .findFirst();
        if(definition.isEmpty()) return false;
        if(!wordModel.definitions.remove(definition.get())) return false;
        writeWordModel(getFilePath(word, language), wordModel);
        return true;
    }

    public String translateWord(String word, String fromLanguage, String toLanguage) {
        Word wordModel = readWordModel(getFilePath(word, fromLanguage), fromLanguage);
//            if(toLanguage.equals("ro")) result = wordModel.word;
//            if(toLanguage.equals("en")) result = wordModel.word_en;
        String toLanguageKey = "word_" + toLanguage;
        if (!wordModel.translations.containsKey(toLanguageKey)) return "word is not found";
        return wordModel.translations.get(toLanguageKey);
    }

    public String translateSentence(String sentence, String fromLanguage, String toLanguage) {
        StringBuilder builder = new StringBuilder();
        String regex = "[\\p{L}\\p{M}]+(?:\\p{P}[\\p{L}\\p{M}]+)*|[\\p{P}\\p{S}\\s]";
        String[] parts = Pattern.compile(regex).matcher(sentence).results().map(MatchResult::group).toArray(String[]::new);
        for(String word : parts) {
            if(Pattern.matches("\\p{IsPunctuation}", word) || Pattern.matches("\\s", word)) {
                builder.append(word);
                continue;
            }
            builder.append(this.translateWord(word, fromLanguage, toLanguage));
        }
        return builder.toString();
    }

    public List<Definition> getDefinitionsForWord(String word, String language) {
        Word wordModel = readWordModel(getFilePath(word, language), language);
        return wordModel.definitions.stream()
                .sorted(Comparator.comparing(definition -> definition.year))
                .collect(Collectors.toList());
    }

    private String getFilePath(String word, String language) {
        return "src/main/resources/translations/" +  language + "/"  + word + ".json";
    }

    private Word readWordModel(String path, String language) {
        Word model = null;
        try(Reader reader = Files.newBufferedReader(Paths.get(path))) {
            model = gson.fromJson(reader, Word.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    private void writeWordModel(String path, Word model) {
        try(Writer reader = new FileWriter(path)) {
            gson.toJson(model, reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
