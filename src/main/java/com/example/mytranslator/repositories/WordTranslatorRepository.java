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

    //reads json files to list
    public List<Word> getWordsCollection(String path) {
        List<Word> result = new ArrayList<>();
        //creates object representation of directory
        File directory = new File(path);
        //iterates over all files inside directory
        for(File file : directory.listFiles()) {
            //checks if current File is a file
            if(file.isFile()) {
                //read word model from file
                Word word = readWordModel(file.getPath());
                //adds to list
                result.add(word);
            }
        }
        return result;
    }

    //gets word model for requested word and language
    public String getWordModel(String word, String language){
        Word model = readWordModel(getFilePath(word, language));
        if(model == null) return "word is not found";
        return model.toString();
    }

    //adds new word
    public boolean addWord(Word word, String language){
        String fileName = getFilePath(word.word, language);
        return writeWordModel(fileName, word);

    }

    //deletes word
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

    //adds definition to specified word and language
    public boolean addDefinitionForWord(String word, String language, Definition definition){
        String fileName = getFilePath(word, language);
        Word wordModel = readWordModel(fileName);
        if(!wordModel.definitions.add(definition)) return false;
        return writeWordModel(fileName, wordModel);
    }

    //removes definition to specified word and language
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

    //translates specified word
    public String translateWord(String word, String fromLanguage, String toLanguage) {
        String result = null;
        Word wordModel = readWordModel(getFilePath(word, fromLanguage));
        if(toLanguage.equals("ro")) result = wordModel.word;
        if(toLanguage.equals("en")) result = wordModel.word_en;
        if(result == null) return "word is not found";
        return result;
    }

    //translates sentence
    public String translateSentence(String sentence, String fromLanguage, String toLanguage) {
        StringBuilder builder = new StringBuilder();
        String[] parts = splitSentence(sentence);
        for(String word : parts) {
            if(Pattern.matches("\\p{IsPunctuation}", word) || Pattern.matches("\\s", word)) {
                builder.append(word);
                continue;
            }
            builder.append(this.translateWord(word, fromLanguage, toLanguage));
        }
        return builder.toString();
    }

    //return definition for requested word
    public List<Definition> getDefinitionsForWord(String word, String language) {
        // About streams https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html
        Word wordModel = readWordModel(getFilePath(word, language));
        return wordModel.definitions.stream() // creates stream from definition list
                .sorted(Comparator.comparing(definition -> definition.year))  //sorts elements of stream by year property
                .collect(Collectors.toList());  //collects all elements into list
    }

    public List<String> translateSentenceWithSynonyms(String sentence, String fromLanguage, String toLanguage) {
        int size = 3;
        //creates arraylist and initializes with string builders
        List<StringBuilder> sbs = new ArrayList<>() {{
            add(new StringBuilder());
            add(new StringBuilder());
            add(new StringBuilder());
        }};
        //get array of words in the sentence
        String[] parts = splitSentence(sentence);
        //iterate over the array
        for (String word : parts) {
            //check if the currently iterated word is an actual word and not a punctuation or a white space
            if(!Pattern.matches("\\p{IsPunctuation}", word) && !Pattern.matches("\\s", word)) {
                Word model = readWordModel(getFilePath(word, fromLanguage));
                //get translation of the current word
                String translation = "";
                if(toLanguage.equals("ro")) translation = model.word;
                if(toLanguage.equals("en")) translation = model.word_en;
                //read translated word's model
                Word translatedWord = readWordModel(getFilePath(translation, toLanguage));
                //container object to avoid null pointer exception
                final Optional<Definition> synonyms = translatedWord.definitions
                        .stream() //get stream of definitions
                        .filter(d -> d.dictType.equals("synonyms"))  // filters every definition of the word using a predicate (a non-interfering, stateless predicate to apply to each element to determine if it should be included)
                        .findFirst(); //gets the first definition found by the filter method
                //checks the container if it contains the required object
                if(synonyms.isPresent()) {
                    //retrieves the definition from the container
                    Definition definition = synonyms.get();
                    //checks if there are less than 3 synonyms for the word and if so, removes one string from the resulting list
                    if(definition.text.size() < size) {
                        size = definition.text.size();
                        sbs.remove(size);
                    }
                    //for each string in the resulting list adds a word from the synonyms array
                    for (int i = 0; i < size; i++) {
                        sbs.get(i).append(definition.text.get(i));
                    }
                }
            } else {
                //if the currently iterated word is a punctuation or a white space then for each string in the resulting list adds that character
                for (int i = 0; i < size; i++) {
                    sbs.get(i).append(word);
                }
            }
        }
        //converts list of string builders to list of strings
        return sbs.stream().map(StringBuilder::toString).collect(Collectors.toList());
    }

    //exports all files in specified language directory into single file
    public void exportJson(String language) {
        //creates new file instance
        File exportFile = new File("src/main/resources/translations/export_" + language + ".json");
        try (FileWriter writer = new FileWriter(exportFile)){
            //creates new file in file system
            exportFile.createNewFile();
            //read the entire directory for specified language
            File directory = new File("src/main/resources/translations/" + language);
            List<Word> words = new ArrayList<>();
            //iterate over files in the directory
            for(File file : directory.listFiles()) {
                //read each file into model
                Word word = readWordModel(file.getAbsolutePath());
                words.add(word);
            }
            //sorts words
            words.sort(Comparator.comparing(word -> word.word));
            gson.toJson(words, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //constructs path to file
    private String getFilePath(String word, String language) {
        return "src/main/resources/translations/" +  language + "/"  + word + ".json";
    }

    //reads word model by path
    private Word readWordModel(String path) {
        Word model = null;
        //object like Reader and Writer must be close when work with them is done to free files on which they were opened
        //to avoid explicit call to close method here try-with-resources construction is used
        //docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        try(Reader reader = Files.newBufferedReader(Paths.get(path))) {
            model = gson.fromJson(reader, Word.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    //writes word model
    private boolean writeWordModel(String path, Word model) {
        //same as for the readWordModel
        try(Writer writer = new FileWriter(path)) {
            gson.toJson(model, writer);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    //splits sentence into parts
    //Ex: "Hello, word!" will result into ["Hello", ",", " ", "world", "!"]
    private String[] splitSentence(String sentence) {
        //[\\p{L}\\p{M}] - match any character  (\p{L} - matches Unicode character, \p{M} - a character intended to be combined with another character)
        // + match 1 or more of the preceding character
        //(?:) groups multiple characters together
        //\p{P} matches punctuation character
        // * match 0 or more of the preceding character
        // | Acts like a boolean OR. Matches the expression before or after the |.
        // \p{S} matches a white space
        //https://www.regular-expressions.info/unicode.html#prop
        //https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
        //https://regexr.com/
        String regex = "[\\p{L}\\p{M}]+(?:\\p{P}[\\p{L}\\p{M}]+)*|[\\p{P}\\p{S}\\s]";
        //A compiled representation of a regular expression.
        //A regular expression, specified as a string, must first be compiled into an instance of this class. The resulting pattern can then be used to create a Matcher object that can match arbitrary character sequences against the regular expression.
        return Pattern.compile(regex)
                .matcher(sentence)  //Creates a matcher that will match the given input against this pattern
                .results()  //Returns a stream of match results for each subsequence of the input sequence that matches the pattern.
                .map(MatchResult::group)  //Returns the input subsequence matched by the previous match.
                .toArray(String[]::new); //converts to array of streams
    }
}
