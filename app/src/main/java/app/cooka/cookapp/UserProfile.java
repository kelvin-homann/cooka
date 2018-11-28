package app.cooka.cookapp;

enum EProfileLocation {
    Local,
    Remote,
    RemoteFacebook,
    RemoteGoogle
}

public class UserProfile {

    private boolean loggedIn;
    private String username;
    private String email;
    private EProfileLocation profileLocation;

    private static UserProfile instance;

    private UserProfile() {
    }

    public static UserProfile getInstance() {
        if(instance == null)
            instance = new UserProfile();
        return instance;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EProfileLocation getProfileLocation() {
        return profileLocation;
    }

    public void setProfileLocation(EProfileLocation profileLocation) {
        this.profileLocation = profileLocation;
    }
}
