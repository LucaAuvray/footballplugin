package fr.codinbox.footballplugin.language;

import java.util.HashMap;

public class Language {

    private final HashMap<String, String> phrases;

    public Language(HashMap<String, String> phrases) {
        this.phrases = phrases;
    }

    public Language() {
        this(null);
    }
    
    public boolean isComplete() {
        for(LanguageKey languageKey : LanguageKey.values())
            if(!phrases.containsKey(languageKey.getKey()))
                return false;
        return true;
    }

    protected HashMap<String, String> getPhrases() {
        return this.phrases;
    }

    public String getPhrase(String key) {
        return this.phrases.get(key);
    }

    public String getPhrase(LanguageKey key) {
        return getPhrase(key.getKey());
    }
    
}
