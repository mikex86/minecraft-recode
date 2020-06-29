package me.gommeantilegit.minecraft.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class StringTranslate {

    /**
     * String translate instance
     */
    private static StringTranslate INSTANCE;

    /**
     * Array of all languages that are available
     */
    @NotNull
    private final Language[] languages;

    /**
     * The current language to translate into
     */
    @NotNull
    private Language currentLanguage;

    /**
     * Default constructor of {@link StringTranslate}
     * Performs heavy IO Operations (File access)
     */
    public StringTranslate() {
        FileHandle languagesFile = Gdx.files.classpath("lang/languages.txt");
        String[] languageNames = languagesFile.readString("UTF-8")
                .replace("\r", "")
                .split("\n");
        this.languages = new Language[languageNames.length];
        int i = 0;
        for (String languageName : languageNames) {
            this.languages[i++] = new Language(Gdx.files.classpath("lang/" + languageName + ".lang"), languageName);
        }
        this.currentLanguage = getLanguage("en_US"); // Default language
        INSTANCE = this;
    }

    /**
     * Translates translation keys to the {@link #currentLanguage}
     * @see Language#translate(String)
     */
    @NotNull
    public String translate(@NotNull String translationKey){
        return this.currentLanguage.translate(translationKey);
    }

    /**
     * Finds the language with the current name and returns the instance
     * @param languageName the name of the language to find
     * @return the language instance with the specified name
     */
    @NotNull
    private Language getLanguage(@NotNull String languageName) {
        for (Language language : languages) {
            if(language.languageName.equals(languageName))
                return language;
        }
        throw new IllegalStateException("No language called " + languageName + " found!");
    }

    public void setCurrentLanguage(@NotNull Language currentLanguage) {
        this.currentLanguage = currentLanguage;
    }

    @NotNull
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Represents a language that can be translated into
     */
    public static final class Language {

        /**
         * The map containing a translation keys and their parent translations in this language
         */
        @NotNull
        private final HashMap<String, String> translationMap;

        /**
         * The name of the language such as "en_US" or "de_DE"
         */
        @NotNull
        private final String languageName;

        /**
         * @param languageFile the .lang file storing the translations
         * @param languageName the name of the language
         * @throws IllegalStateException if the language file is of invalid format
         */
        public Language(@NotNull FileHandle languageFile, @NotNull String languageName) {
            this.languageName = languageName;
            String[] lines = languageFile.readString("UTF-8").split("\n");
            this.translationMap = new HashMap<>();
            int lineNumber = 0;
            for (String line : lines) {
                if (line.startsWith("#")) continue; // Ignoring comment lines
                String[] args = line.split("=");
                if (args.length != 2) {
                    throw new IllegalStateException("Invalid language file " + languageFile + "! Line: " + lineNumber + " doesn't have to format \"key=value\"! Line: \"" + line + "\"");
                }
                this.translationMap.put(args[0], args[1]);
                lineNumber++;
            }
        }

        /**
         * @param translationKey the translation key to translate into this language
         * @return the translation or if no translation is found, the same translation key.
         */
        @NotNull
        public String translate(@NotNull String translationKey) {
            return this.translationMap.getOrDefault(translationKey, translationKey);
        }
    }

    /**
     * @return the default string translate instance
     */
    @NotNull
    public static StringTranslate getInstance() {
        return INSTANCE;
    }
}
