package com.xranky.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.xranky.model.AppState;
import com.xranky.model.FeedItem;
import com.xranky.model.FeedItemModel;
import com.xranky.model.Ranking;
import com.xranky.model.User;
import com.xranky.twitter.TwitterSupport;
import com.xranky.twitter.XRankyTwitterClient;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import retrofit.client.Response;

public class SQLiteFunction {

	public SQLiteDatabase db;
	com.xranky.model.Ranking rank;

	public static int READ =1;
	public static int WRITE =2;

	public SQLiteFunction(Context ctx,int type){
		SQliteDB dbh = new SQliteDB(ctx);
		rank = new Ranking();
		if(type == READ){
			db = dbh.getReadableDatabase();
		}else{
			db = dbh.getWritableDatabase();
		}
	}

	public void saveFeedItemModel(FeedItemModel[] models){
		try{
			for (int i = 0; i < models.length; i ++) {
				if(models[i] == null) continue;
				Cursor cursor = db.rawQuery("select id from FeedItemB where id = ? ", new String[]{"" + models[i].id});
				ContentValues values = new ContentValues();
				values.put("id", models[i].id);
				values.put("userid", models[i].userid);
				values.put("name", models[i].name);
				values.put("scrname", models[i].screenname);
				values.put("profilepic", models[i].profilepic);
				values.put("status", models[i].status);
				values.put("videourl", models[i].videoUrl);
				values.put("reuser", models[i].reUser);
				values.put("favorites", rank.toK(models[i].favorites));
				values.put("retweets", rank.toK(models[i].retweets));
				values.put("replies", rank.toK(models[i].replies));
				values.put("retweeted", models[i].retweeted);
				values.put("followers", rank.toK(models[i].followers));
				values.put("homeR", rank.home(models[i].favorites, models[i].retweets, models[i].replies, models[i].followers));
				values.put("repliesR", 0);
				values.put("retweetsR", rank.retweets(models[i].retweets, models[i].followers));
				values.put("favoritesR", rank.favorites(models[i].favorites, models[i].followers));
				values.put("ref", rank.ref(models[i].followers));
				values.put("atTime", models[i].date);
				values.put("isFollow", models[i].isFollow);
				if (cursor.getCount() > 0) {
					db.update("FeedItemB", values, "id = ? ", new String[]{"" + models[i].id});
				} else {
					db.insert("FeedItemB", null, values);
				}
			}
		}catch(Exception e){
			Log.e("SaveFeedItemModel",""+e.toString());
		}
	}

	public void deleteFromB(){
		try{
			db.execSQL("delete from FeedItemB ");
		}catch(Exception e){}
	}

	public void updateRef(long id ,int ref){
		db.execSQL("update FeedItem set ref = ? where userid = ? ",new String[]{""+ref,""+id});
	}

	public ArrayList<FeedItem> getFeeds(String view){
		deleteBefore24H();
		ArrayList<FeedItem> items = new ArrayList<FeedItem>();
		long date = new Date().getTime()/1000;
		try{
			String sql = "SELECT * FROM " + view;
			Cursor cursor = db.rawQuery( sql, null);
			if(cursor.moveToFirst()){
				int i = 1;
				do {
					//id, userid,name,status,favorites,retweets,replies,followers
					FeedItem item = new FeedItem();
					item.id = cursor.getLong(0);
					item.userid = cursor.getLong(1);
					item.name = cursor.getString(2);
					item.scrName = cursor.getString(11);
					item.status = cursor.getString(3);
					item.profilepic = cursor.getString(8);
					item.videoURL = cursor.getString(9);
					item.reUser = cursor.getString(14);
					item.favorites = cursor.getString(4);
					item.retweets = cursor.getString(5);
					item.replies = cursor.getString(6);
					item.followers = cursor.getString(7);
					item.retweeted = cursor.getInt(13);
					if((date-cursor.getLong(10)/1000)/(60*60) >= 1)
						item.time = ""+((date-cursor.getLong(10)/1000)/(60*60))+"h";
					else if((date-cursor.getLong(10)/1000)/60 >= 1)
						item.time = ""+((date-cursor.getLong(10)/1000)/60)+"m";
					else
						item.time = ""+((date-cursor.getLong(10)/1000))+"s";
					item.isFollow = cursor.getInt(12);
					items.add(item);
					i ++;
				} while (cursor.moveToNext());
			}
		}catch(Exception e){
			Log.e("getFeeds "+view,""+e.toString());
		}
		return items;
	}

	public void deleteALL(){
		db.execSQL("DELETE FROM FeedItem");
		deleteFromB();
		selectAll();
	}

	public void transfer(){
		try{
			db.execSQL("DELETE FROM FeedItem WHERE id IN(select id from  FeedItemB)");
			db.execSQL("INSERT INTO  FeedItem SELECT * FROM homepageslimit");
			db.execSQL("INSERT INTO  FeedItem SELECT * FROM homelimit");
			db.execSQL("DELETE FROM FeedItemB ");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void login (long userId){
		XRankyTwitterClient apiClient = new XRankyTwitterClient(TwitterSupport.getSession());
		apiClient.userLookup().show(userId, false, new Callback<Response>() {
			@Override
			public void success(Result<Response> result) {
				BufferedReader reader = null;
				StringBuilder sb = new StringBuilder();
				try {
					reader = new BufferedReader(new InputStreamReader(result.response.getBody().in()));
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				String response = sb.toString();
				try {
					JSONArray objArr = new JSONArray(response);
					User user = new User();
					user.setUserId(Long.valueOf(objArr.getJSONObject(0).get("id").toString()));
					user.setProfileImage(objArr.getJSONObject(0).get("profile_image_url").toString());
					user.setScrname(objArr.getJSONObject(0).get("screen_name").toString());
					user.setName(objArr.getJSONObject(0).get("name").toString());
					updateAccount(user);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failure(TwitterException e) {
				e.printStackTrace();
				Log.e("ERROR","Search Faild");
			}
		});
	}

	public boolean isLogin(){
		com.xranky.model.User account = getAccount();
		if (TwitterSupport.getSession() == null){
			return false;
		}
		if (account.getUserId() != TwitterSupport.getSession().getUserId()){
			return false;
		}
		return true;
	}

	public boolean logOut(){
//		User user = new User();
//		updateAccount(user);
		return true;
	}

	public User getAccount(){
		User account = new User();
		try {
			Cursor cursor = db.rawQuery("SELECT * FROM Account",null);
			if(cursor.moveToFirst()){
				account.setUserId(cursor.getLong(1));
				account.setName(cursor.getString(2));
				account.setScrname(cursor.getString(3));
				account.setProfileImage(cursor.getString(4));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return account;
	}

	public void updateAccount (User user) {
		try {
			db.execSQL("UPDATE Account SET userid = "+user.getUserId()+", name = '"+user.getName()+"', scrname = '"+user.getScrname()+"', profilepic = '"+user.getProfileImage()+"' WHERE id = 1 ");
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void updateAppState(AppState app){
		int i = 0;
		String setQuery = "";
		if (!app.appName.isEmpty()) {
			setQuery += "app_name = " + app.appName;
			i ++;
		}
		if (app.requestCount != 0) {

			if (i == 0)
				setQuery += "request_count = " + app.requestCount;
			else
				setQuery += ",request_count = " + app.requestCount;
			i ++;
		}
		if (app.updatedTime != 0) {
			if (i == 0)
				setQuery += "update_time = " + app.updatedTime;
			else
				setQuery += ",update_time = " + app.updatedTime;
			i ++;
		}

		String query = "UPDATE AppState SET "+setQuery+" WHERE id = 1 ";
		try{
			db.execSQL(query);
		}catch (SQLException e){
			e.printStackTrace();
		}
	}

	public boolean checkRequest(){
		int request_count = 0;
		int updatedTime = 0;
		try{
			Cursor cursor = db.rawQuery("SELECT request_count,update_time FROM AppState",null);
			if(cursor.moveToFirst()){
				request_count = cursor.getInt(0);
				updatedTime = Math.round((new Date().getTime() - cursor.getLong(1))/60000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		if(request_count > 13 && updatedTime < 15){
			return false;
		}
		return true;
	}

	public AppState getAppState (){
		AppState app = new AppState();
		try{
			Cursor cursor = db.rawQuery("SELECT * FROM AppState",null);
			if(cursor.moveToFirst()){
				app.appName = cursor.getString(1);
				app.requestCount = cursor.getInt(2);
				app.updatedTime = cursor.getLong(3);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return app;
	}

	public void updateReplies (FeedItemModel model, int count) {
		try {
			updateRepliesR(model.id, rank.replies(model.replies, model.followers));
			db.execSQL("UPDATE FeedItem SET replies = "+count+" WHERE userid =" + model.userid);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void updateRepliesR(long id, double rank){
		try {
			db.execSQL("UPDATE FeedItem SET repliesR = "+rank+" WHERE id =" + id);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public int getCount(){
		int count = 0;
		try{
			Cursor cursor = db.rawQuery("SELECT COUNT(ref) FROM FeedItem",null);
			if(cursor.moveToFirst()){
				count = cursor.getInt(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return count;
	}

	public long getMaxIdUser(long userid){
		long id = 1;
		try{
			Cursor cursor = db.rawQuery("SELECT MAX(id) FROM FeedItem WHERE userid = "+userid,null);
			if(cursor.moveToFirst()){
				id = cursor.getLong(0);
			}
		}catch(Exception e){}
		return id;
	}

	public String getScreenNameUser(long userid){
		String name = "";
		try{
			Cursor cursor = db.rawQuery("SELECT MAX(name) FROM FeedItem WHERE userid = "+userid,null);
			if(cursor.moveToFirst()){
				name = cursor.getString(0);
			}
		}catch(Exception e){}
		return name;
	}

	public void updateIsFollow(String userid){
		try{
			ContentValues values = new ContentValues();
			values.put("isFollow",1);

			db.update("FeedItem", values, "userid = ? ", new String[]{""+userid});

		}catch(Exception e){
			Log.e("SaveFeedItemModel",""+e.toString());
		}
	}

	public void deleteBefore24H(){
		try{
			long date = System.currentTimeMillis()-(60*60*1000);
			db.execSQL("DELETE FROM FeedItem WHERE atTime < "+date);
		}catch(Exception e){
			Log.e("deleteBefore24H",""+e.toString());
		}
	}

	public void selectAll(){
		try{
			db.rawQuery("SELECT * FROM home ",null);
			db.rawQuery("SELECT * FROM homepages ",null);

			db.rawQuery("SELECT * FROM replies ",null);
			db.rawQuery("SELECT * FROM repliespages ",null);

			db.rawQuery("SELECT * FROM retweets ",null);
			db.rawQuery("SELECT * FROM retweetspages ",null);

			db.rawQuery("SELECT * FROM favorites ",null);
			db.rawQuery("SELECT * FROM favoritespages ",null);
		}catch(Exception e){
			Log.e("selectAll",""+e.toString());
		}
	}
}
