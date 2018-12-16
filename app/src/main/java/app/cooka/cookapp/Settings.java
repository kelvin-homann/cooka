package app.cooka.cookapp;

public class Settings {

    private long currentLanguageId = 1031;

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
}
