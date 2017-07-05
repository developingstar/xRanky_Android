package com.xranky.model;

public class FeedItemModel {
	
	public long id =0; // tweets id
	public long userid =0; // user id
	public String name =""; // user name
	public String profilepic =""; // profile image
	public String status =""; // tweet text
	public String videoUrl = ""; //image or video URL
	public int favorites =0; // like count
	public int retweets =0; // retweets count
	public int replies =0; // reply count
	public int followers =0; // followers count
	public double homeR =0; //
	public double repliesR=0; //
	public double retweetsR=0; //
	public double favoritesR =0; //
	public int retweeted = 0;
	public String reUser = "";
	public int ref =1; //
	public String date; // tweets created date time
	public String screenname =""; // screen name
	public int isFollow = 0; // is following

}
