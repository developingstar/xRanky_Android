package com.xranky.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQliteDB extends SQLiteOpenHelper{

	public SQliteDB(Context context) {
		super(context, "xrankymmrdb.db", null, 3);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		SQliteHelp help = new SQliteHelp();

		db.execSQL("create table if not exists AppState (id long primary key,app_name text,request_count int,update_time long)");
		db.execSQL("insert into  AppState values(1,'xRanky',0,144);");

		db.execSQL("create table if not exists Account (id long primary key,userid long,name text,scrname text,profilepic text)");
		db.execSQL("insert into  Account values(1,199367,'Star','K','');");

		db.execSQL("create table if not exists FeedItem (id long primary key,userid long,name text,profilepic text,status text,videourl text,favorites text,retweets text,replies text,followers text,homeR double,repliesR double,retweetsR double,favoritesR double,ref integer default 1,atTime datetime,scrname text,isFollow Integer default 0,retweeted int,reuser text)");
		db.execSQL("create table if not exists FeedItemB (id long primary key,userid long,name text,profilepic text,status text,videourl text,favorites text,retweets text,replies text,followers text,homeR double,repliesR double,retweetsR double,favoritesR double,ref integer default 1,atTime datetime,scrname text,isFollow Integer default 0,retweeted int,reuser text)");

		db.execSQL("create view if not exists "+help.homelimit+" as select id, userid,name,profilepic,status,videourl,favorites,retweets,replies,followers,homeR,repliesR,retweetsR,favoritesR,ref,atTime as time,scrname,isFollow,retweeted,reuser from FeedItemB where ref = 1 order by homeR DESC,retweetsR DESC,favoritesR DESC,repliesR DESC LIMIT 100");
		db.execSQL("create view if not exists "+help.homepageslimit+" as select id, userid,name,profilepic,status,videourl,favorites,retweets,replies,followers,homeR,repliesR,retweetsR,favoritesR,ref,atTime as time,scrname,isFollow,retweeted,reuser from FeedItemB where ref = 2 order by homeR DESC,retweetsR DESC,favoritesR DESC,repliesR DESC LIMIT 100");
		
		db.execSQL("create view if not exists "+help.home+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 1 order by homeR DESC,retweetsR DESC,favoritesR DESC,repliesR DESC ");
		db.execSQL("create view if not exists "+help.homepages+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 2 order by homeR DESC,retweetsR DESC,favoritesR DESC,repliesR DESC ");

//		db.execSQL("create view if not exists "+help.replies+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 1 order by repliesR DESC,homeR DESC,retweetsR DESC,favoritesR DESC ");
//		db.execSQL("create view if not exists "+help.repliespages+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 2 order by repliesR DESC,homeR DESC,retweetsR DESC,favoritesR DESC ");
//
//		db.execSQL("create view if not exists "+help.retweets+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 1 order by retweetsR DESC,homeR DESC,favoritesR DESC,repliesR DESC ");
//		db.execSQL("create view if not exists "+help.retweetspages+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 2 order by retweetsR DESC,homeR DESC,favoritesR DESC,repliesR DESC ");
//
//		db.execSQL("create view if not exists "+help.favorites+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 1 order by favoritesR DESC,homeR DESC,retweetsR DESC,repliesR DESC ");
//		db.execSQL("create view if not exists "+help.favoritespages+" as select id, userid,name,status,favorites,retweets,replies,followers,profilepic,videourl,atTime as time,scrname,isFollow,retweeted,reuser from FeedItem where ref = 2 order by favoritesR DESC,homeR DESC,retweetsR DESC,repliesR DESC ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
