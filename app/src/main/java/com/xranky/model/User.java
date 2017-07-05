package com.xranky.model;

/**
 * Created by Star on 6/17/2016.
 */
public class User {
    private long userId;
    private String name;
    private String scrname;
    private String profileImage;

    public User(){
        userId = 199367;
        name = "Star";
        scrname = "K";
        profileImage = "";
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScrname() {
        return scrname;
    }

    public void setScrname(String scrname) {
        this.scrname = scrname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
