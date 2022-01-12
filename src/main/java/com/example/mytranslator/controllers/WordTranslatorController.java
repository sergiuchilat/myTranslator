package com.example.mytranslator.controllers;

import com.example.mytranslator.models.Definition;
import com.example.mytranslator.models.Word;
import com.example.mytranslator.repositories.WordTranslatorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WordTranslatorController {

    private WordTranslatorRepository wordTranslatorRepository = new WordTranslatorRepository();

    @GetMapping(path = "translate/word/{language}/{word}")
    public String translateWord(@PathVariable String word, @PathVariable String language){
        return wordTranslatorRepository.getWordModel(word, language);
    }

    @PostMapping(path = "translate/word/{language}")
    public boolean addWord(@RequestBody Word word, @PathVariable String language){
        return wordTranslatorRepository.addWord(word, language);
    }

    @DeleteMapping(path = "translate/word/{language}/{word}")
    public boolean deleteWord(@PathVariable String word, @PathVariable String language){
        return wordTranslatorRepository.deleteWord(word, language);
    }

    @PostMapping(path = "translate/word/{language}/{word}")
    public boolean addDefinitionForWord(@PathVariable String word, @PathVariable String language, @RequestBody Definition definition){
        return wordTranslatorRepository.addDefinitionForWord(word, language, definition);
    }

    @DeleteMapping(path = "translate/word/{language}/{word}/{dictionary}")
    public boolean removeDefinition(@PathVariable String word, @PathVariable String language, @PathVariable String dictionary){
        return wordTranslatorRepository.removeDefinition(word, language, dictionary);
    }

    @GetMapping(path = "translate/word/{word}/{fromLanguage}/{toLanguage}")
    public String translateWord(@PathVariable String word, @PathVariable String fromLanguage, @PathVariable String toLanguage) {
        return wordTranslatorRepository.getWordModel(word, fromLanguage, toLanguage);
    }

    @GetMapping(path = "translate/sentence/{sentence}/{fromLanguage}/{toLanguage}")
    public String translateSentence(@PathVariable String sentence, @PathVariable String fromLanguage, @PathVariable String toLanguage) {
        return wordTranslatorRepository.translateSentence(sentence, fromLanguage, toLanguage);
    }

    @GetMapping(path = "translate/definitions/{word}/{language}")
    public List<Definition> getDefinitionForWord(@PathVariable String word, @PathVariable String language) {
        return wordTranslatorRepository.getDefinitionsForWord(word, language);
    }

    @GetMapping(path = "translate/sentence-with-synonyms/{sentence}/{fromLanguage}/{toLanguage}")
    public List<String> translateSentenceWithSynonyms(@PathVariable String sentence, @PathVariable String fromLanguage, @PathVariable String toLanguage) {
        return wordTranslatorRepository.translateSentenceWithSynonyms(sentence, fromLanguage, toLanguage);
    }

    @GetMapping(path = "export/{language}")
    public void exportJson(@PathVariable String language) {
        wordTranslatorRepository.exportJson(language);
    }
}
