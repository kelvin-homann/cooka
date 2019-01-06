package app.cooka.cookapp;

public class Settings {

    private long currentLanguageId = 1031;
    private String currentLanguageCode = "de-de";
    private String currentLocale = "de";

    private Settings() {
    }

    public static class Factory {

        private static Settings instance;
        public static Settings getInstance() {
            if(instance == null)
                instance = new Settings();
            return instance;
        }
    }

    public static Settings getInstance() {
        return Factory.getInstance();
    }

    public long getCurrentLanguageId() {
        return currentLanguageId;
    }

    public void setCurrentLanguageId(long languageId) {
        currentLanguageId = languageId;
    }

    public String getCurrentLanguageCode() {
        return currentLanguageCode;
    }

    public void setCurrentLanguageCode(String currentLanguageCode) {
        this.currentLanguageCode = currentLanguageCode;
    }

    public String getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(String currentLocale) {
        this.currentLocale = currentLocale;
    }
}
