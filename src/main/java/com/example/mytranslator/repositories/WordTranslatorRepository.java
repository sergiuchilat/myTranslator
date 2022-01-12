package com.example.mytranslator.repositories;

import com.example.mytranslator.models.Definition;
import com.example.mytranslator.models.JsonWord;
import com.example.mytranslator.models.Word;
import com.example.mytranslator.models.JsonWordWrapper;
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
    public String getWordModel(String word, String language){
        Word model = getWord(word, language);
        if(model == null) return "word is not found";
        return model.toString();
    }

    public boolean addWord(Word word, String language){
        String fileName = getFilePath(word.word, language);
        return writeWordModel(fileName, word);

    }

    public boolean deleteWord(String word, String language){
        String fileName = getFilePath(word, language);
        try {
            File file = new File(fileName);
            file.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addDefinitionForWord(String word, String language, Definition definition){
        String fileName = getFilePath(word, language);
        Word wordModel = readWordModel(fileName);
        if(!wordModel.definitions.add(definition)) return false;
        return writeWordModel(fileName, wordModel);
    }

    public boolean removeDefinition(String word, String language, String dictionary) {
        Word wordModel = readWordModel(getFilePath(word, language));
        Optional<Definition> definition = wordModel.definitions.stream()
                .filter(def -> def.dict.equals(dictionary))
                .findFirst();
        if(definition.isEmpty()) return false;
        if(!wordModel.definitions.remove(definition.get())) return false;
        writeWordModel(getFilePath(word, language), wordModel);
        return true;
    }

    public String getWordModel(String word, String fromLanguage, String toLanguage) {
        Word wordModel = readWordModel(getFilePath(word, fromLanguage));
//            if(toLanguage.equals("ro")) result = wordModel.word;
//            if(toLanguage.equals("en")) result = wordModel.word_en;
        String toLanguageKey = "word_" + toLanguage;
        if (!wordModel.translations.containsKey(toLanguageKey)) return "word is not found";
        return wordModel.translations.get(toLanguageKey);
    }

    public String translateSentence(String sentence, String fromLanguage, String toLanguage) {
        StringBuilder builder = new StringBuilder();
        String[] parts = splitSentence(sentence);
        for(String word : parts) {
            if(Pattern.matches("\\p{IsPunctuation}", word) || Pattern.matches("\\s", word)) {
                builder.append(word);
                continue;
            }
            builder.append(this.getWordModel(word, fromLanguage, toLanguage));
        }
        return builder.toString();
    }

    public List<Definition> getDefinitionsForWord(String word, String language) {
        Word wordModel = readWordModel(getFilePath(word, language));
        return wordModel.definitions.stream()
                .sorted(Comparator.comparing(definition -> definition.year))
                .collect(Collectors.toList());
    }

    public List<String> translateSentenceWithSynonyms(String sentence, String fromLanguage, String toLanguage) {
        int size = 3;
        List<StringBuilder> sbs = new ArrayList<>() {{
            add(new StringBuilder());
            add(new StringBuilder());
            add(new StringBuilder());
        }};
        String[] parts = splitSentence(sentence);
        for (String word : parts) {
            if(!Pattern.matches("\\p{IsPunctuation}", word) && !Pattern.matches("\\s", word)) {
                Word model = readWordModel(getFilePath(word, fromLanguage));
                String translation = model.translations.get("word_" + toLanguage);
                Word translatedWord = readWordModel(getFilePath(translation, toLanguage));
                final Optional<Definition> synonyms = translatedWord.definitions
                        .stream()
                        .filter(d -> d.dictType.equals("synonyms"))
                        .findFirst();
                if(synonyms.isPresent()) {
                    Definition definition = synonyms.get();
                    if(definition.text.size() < size) {
                        size = definition.text.size();
                        sbs.remove(size);
                    }
                    for (int i = 0; i < size; i++) {
                        sbs.get(i).append(definition.text.get(i));
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    sbs.get(i).append(word);
                }
            }
        }
        return sbs.stream().map(StringBuilder::toString).collect(Collectors.toList());
    }

    public void exportJson(String language) {
        File exportFile = new File("src/main/resources/translations/export_" + language + ".json");
        try {
            exportFile.createNewFile();
            JsonWordWrapper jsonWordWrapper = new JsonWordWrapper();
            File directory = new File("src/main/resources/translations/" + language);
            List<Word> words = new ArrayList<>();
            for(File file : directory.listFiles()) {
                words.add(readWordModel(file.getAbsolutePath()));
            }
            jsonWordWrapper.words.addAll(words.stream().map(JsonWord::new).collect(Collectors.toList()));
            FileWriter writer = new FileWriter(exportFile);
            gson.toJson(jsonWordWrapper, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(String word, String language) {
        return "src/main/resources/translations/" +  language + "/"  + word + ".json";
    }

    private Word readWordModel(String path) {
        Word model = null;
        try(Reader reader = Files.newBufferedReader(Paths.get(path))) {
            model = gson.fromJson(reader, Word.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    private boolean writeWordModel(String path, Word model) {
        try(Writer writer = new FileWriter(path)) {
            gson.toJson(model, writer);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private Word getWord(String word, String language) {
        return readWordModel(getFilePath(word, language));
    }

    private String[] splitSentence(String sentence) {
        String regex = "[\\p{L}\\p{M}]+(?:\\p{P}[\\p{L}\\p{M}]+)*|[\\p{P}\\p{S}\\s]";
        return Pattern.compile(regex)
                .matcher(sentence)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
    }
}
