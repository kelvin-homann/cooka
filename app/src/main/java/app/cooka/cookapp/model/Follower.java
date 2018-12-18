package app.cooka.cookapp.model;

public class Follower {

    private long ofuserId;
    private long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private long profileImageId;
    private String profileImageFileName;
    private int followerCount = 0;
    private int followeeCount = 0;
    private int verifiedState = 0;

    public long getOfuserId() {
        return ofuserId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public long getProfileImageId() {
        return profileImageId;
    }

    public String getProfileImageFileName() {
        return profileImageFileName;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFolloweeCount() {
        return followeeCount;
    }

    public int getVerifiedState() {
        return verifiedState;
    }
}
