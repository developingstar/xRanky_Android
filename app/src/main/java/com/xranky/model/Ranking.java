package com.xranky.model;

import android.util.Log;

import java.text.DecimalFormat;

public class Ranking {

	public double home(double favorites,double retweets,double replies,double followers){
		double rank = 0.0;
		try{
			DecimalFormat twoDForm = new DecimalFormat("#.####");
			double second = Double.valueOf(twoDForm.format(Math.log10(followers)));
			rank = (favorites+retweets+replies) / second;
			rank =  Double.valueOf(twoDForm.format(rank));
		}catch(Exception e){
			Log.e("home",""+e.toString());
		}
		return rank;
	}
	
	public double replies(int replies,int followers){
		double rank = 0;
		try{
			DecimalFormat twoDForm = new DecimalFormat("#.####");
			double second = Double.valueOf(twoDForm.format(Math.log10(followers)));
			rank = replies / second;
			rank =  Double.valueOf(twoDForm.format(rank));
		}catch(Exception e){
			Log.e("replies",""+e.toString());
		}
		return rank;
	}
	
	public double retweets(int retweets,int followers){
		double rank = 0;
		try{
			DecimalFormat twoDForm = new DecimalFormat("#.####");
			double second = Double.valueOf(twoDForm.format(Math.log10(followers)));
			rank =retweets / second;
			rank =  Double.valueOf(twoDForm.format(rank));
		}catch(Exception e){
			Log.e("retweets",""+e.toString());
		}
		return rank;
	}
	
	public double favorites(int favorites,int followers){
		double rank = 0;
		try{
			DecimalFormat twoDForm = new DecimalFormat("#.####");
			double second = Double.valueOf(twoDForm.format(Math.log10(followers)));
			rank = favorites / second;
			rank =  Double.valueOf(twoDForm.format(rank));
		}catch(Exception e){
			Log.e("favorites",""+e.toString());
		}
		return rank;
	}
	
	public int ref(int followers){
		int ref = 1;
		try{
			if(followers >= 10000)ref =2;
		}catch(Exception e){
			
		}
		return ref;
	}

	public String toK(int value){
		String k = ""+value;
		double tmp1 = 0.0;
		if(value >= 1000000){
			tmp1 = value/1000000;
			DecimalFormat twoDForm = new DecimalFormat("#.#");
			double tmp2 = Double.valueOf(twoDForm.format((value%1000000) * 0.000001));
			k = (tmp1 + tmp2)+"M";
		}else
		if(value >= 1000){
			tmp1 = value/1000;
			DecimalFormat twoDForm = new DecimalFormat("#.#");
			double tmp2 = Double.valueOf(twoDForm.format((value%1000) * 0.001));
			k = (tmp1 + tmp2)+"K";
		}
		return k;
	}
	
	public static boolean isDate24H(long date){
		long d = ((System.currentTimeMillis()-date)/1000);
		return d <= (60*60);
	}
}
