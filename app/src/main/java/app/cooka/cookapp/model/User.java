package app.cooka.cookapp.model;

import java.util.Date;

public class User {

    private final long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private boolean confirmedEmailAddress;
    private byte verifiedState;
    private int userRights;

    private ELinkedProfileType linkedProfileType;
    private String linkedProfileUserId;

    private Date joinedDateTime;
    private Date lastActiveDateTime;
    private Date lastRecipeCreatedDateTime;
    private Date lastCollectionEditedDateTime;
    private Date lastCookModeUsedDateTime;

    private int viewedCount;
    private int followedCount;
    private int followingCount;


    public User(final long userId, String userName, String emailAddress, int userRights) {

        this.userId = userId;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.userRights = userRights;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean getConfirmedEmailAddress() {
        return confirmedEmailAddress;
    }

    public void setConfirmedEmailAddress(boolean confirmedEmailAddress) {
        this.confirmedEmailAddress = confirmedEmailAddress;
    }

    public byte getVerifiedState() {
        return verifiedState;
    }

    public void setVerifiedState(byte verifiedState) {
        this.verifiedState = verifiedState;
    }

    public int getUserRights() {
        return userRights;
    }

    public void setUserRights(int userRights) {
        this.userRights = userRights;
    }

    public void addUserRights(int rightsMask) {
        this.userRights |= rightsMask;
    }

    public void removeUserRights(int rightsMask) {
        this.userRights ^= rightsMask;
    }

    public ELinkedProfileType getLinkedProfileType() {
        return linkedProfileType;
    }

    public String getLinkedProfileUserId() {
        return linkedProfileUserId;
    }

    public void setLinkedProfile(ELinkedProfileType linkedProfileType, String linkedProfileUserId) {
        this.linkedProfileType = linkedProfileType;
        this.linkedProfileUserId = linkedProfileUserId;
    }

    public Date getJoinedDateTime() {
        return joinedDateTime;
    }

    public void setJoinedDateTime(Date joinedDateTime) {
        this.joinedDateTime = joinedDateTime;
    }

    public Date getLastActiveDateTime() {
        return lastActiveDateTime;
    }

    public void setLastActiveDateTime(Date lastActiveDateTime) {
        this.lastActiveDateTime = lastActiveDateTime;
    }

    public Date getLastRecipeCreatedDateTime() {
        return lastRecipeCreatedDateTime;
    }

    public void setLastRecipeCreatedDateTime(Date lastRecipeCreatedDateTime) {
        this.lastRecipeCreatedDateTime = lastRecipeCreatedDateTime;
    }

    public Date getLastCollectionEditedDateTime() {
        return lastCollectionEditedDateTime;
    }

    public void setLastCollectionEditedDateTime(Date lastCollectionEditedDateTime) {
        this.lastCollectionEditedDateTime = lastCollectionEditedDateTime;
    }

    public Date getLastCookModeUsedDateTime() {
        return lastCookModeUsedDateTime;
    }

    public void setLastCookModeUsedDateTime(Date lastCookModeUsedDateTime) {
        this.lastCookModeUsedDateTime = lastCookModeUsedDateTime;
    }

    public int getViewedCount() {
        return viewedCount;
    }

    public void setViewedCount(int viewedCount) {
        this.viewedCount = viewedCount;
    }

    public int getFollowedCount() {
        return followedCount;
    }

    public void setFollowedCount(int followedCount) {
        this.followedCount = followedCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}
