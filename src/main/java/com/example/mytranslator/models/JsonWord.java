package com.example.mytranslator.models;

import java.util.ArrayList;
import java.util.Set;

public class JsonWord {
	public String word;
	public String type;
	public ArrayList<String> singular;
	public ArrayList<String> plural;
	public Set<Definition> definitions;

	public JsonWord(Word word) {
		this.word = word.word;
		this.type = word.type;
		this.singular = word.singular;
		this.plural = word.plural;
		this.definitions = word.definitions;
	}
}
