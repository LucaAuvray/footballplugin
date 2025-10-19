package fr.codinbox.footballplugin.language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.codinbox.footballplugin.FootballPlugin;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Logger;

public class LanguageManager {

    private static final Logger LOGGER = Bukkit.getLogger();

    private final File languageFile;

    private Language currentLanguage;

    public LanguageManager(File languageFile) throws IOException {
        LOGGER.info("[Football] Initializing LanguageManager");

        this.languageFile = languageFile;

        //
        // File existence verification
        //

        // File isn't readable
        if(languageFile.exists() && !languageFile.canRead())
            throw new IOException("Unable to read language.json file");

        //
        // Load language.json file
        //

        Language language;

        // If the language file is not found, create one
        if(!languageFile.exists()) {
            language = new Language(LanguageKey.mapDefaultValues());
            saveLanguageFile(language);
        }

        // If the file exists, load it
        else {
            final Gson gson = new Gson();
            language = gson.fromJson(new InputStreamReader(new FileInputStream(this.languageFile), StandardCharsets.UTF_8), Language.class);
        }

        language = new Language(LanguageKey.mapDefaultValues());

        // Setting loaded or create language into the current language
        this.currentLanguage = language;

        // Checking if the language is actually complete, if not complete it and save it
        if(!currentLanguage.isComplete()) {
            LOGGER.info("[Football] Language file not complete, auto fix in progress...");
            autofixLanguage();
            saveLanguageFile(this.currentLanguage);
            LOGGER.fine("[Football] Auto language fix complete!");
        }

        LOGGER.fine("[Football] Language successfully loaded!");
    }

    private void saveLanguageFile(Language language) throws IOException {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Writer writer = new OutputStreamWriter(new FileOutputStream(this.languageFile), StandardCharsets.UTF_8);
        writer.write(gson.toJson(language));
        writer.flush();
        writer.close();
        LOGGER.info("[Football] Language file saved!");
    }

    private void autofixLanguage() {
        HashMap<String, String> currentPhrases = this.currentLanguage.getPhrases();
        HashMap<String, String> newPhrases = new HashMap<>();
        for(LanguageKey languageKey : LanguageKey.values()) {
            String key = languageKey.getKey();
            String defaultValue = languageKey.getDefaultValue();
            newPhrases.put(key, currentPhrases.getOrDefault(key, defaultValue));
        }
        this.currentLanguage = new Language(newPhrases);
    }

    public String getPhrase(LanguageKey key) {
        return this.currentLanguage.getPhrase(key);
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static class Phrase {

        private LanguageManager languageManager;
        private LanguageKey key;
        private HashMap<String, String> replacements;

        private String finalString;

        public Phrase(LanguageManager languageManager, LanguageKey key, HashMap<String, String> replacements) {
            this.languageManager = languageManager;
            this.key = key;
            this.replacements = replacements;
        }

        public Phrase(LanguageManager languageManager, LanguageKey key) {
            this(languageManager, key, new HashMap<>());
        }

        public Phrase(LanguageKey key) {
            this(FootballPlugin.INSTANCE.getLanguageManager(), key, new HashMap<>());
        }

        public Phrase replaceVar(String key, String value) {
            this.replacements.put(key, value);
            return this;
        }

        public String toString() {
            finalString = this.languageManager.getPhrase(this.key);
            replacements.forEach((key, value) -> {
                finalString = finalString.replaceAll("%" + key + "%", value);
            });
            return finalString;
        }

    }

}
