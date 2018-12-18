package app.cooka.cookapp.model;

public class Followee {

    private EFolloweeType type;
    private long ofuserId;
    private long id;
    private String displayName;
    private String detail1;
    private String detail2;
    private long imageId;
    private String imageFileName;
    private int followerCount;
    private int followeeCount;

    public EFolloweeType getType() {
        return type;
    }

    public long getOfUserId() {
        return ofuserId;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDetail1() {
        return detail1;
    }

    public String getDetail2() {
        return detail2;
    }

    public long getImageId() {
        return imageId;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFolloweeCount() {
        return followeeCount;
    }
}
