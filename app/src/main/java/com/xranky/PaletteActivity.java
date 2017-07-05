package com.xranky;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
//import android.support.v7.graphics.Palette;

public class PaletteActivity extends Activity {
	TextView textView1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_palette);
		textView1 = (TextView)findViewById(R.id.textView1);
		File imgFile = new  File("/sdcard/test.jpg");
		 Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
       
		/*Palette palette  = Palette.generate(myBitmap);
		Palette.Swatch swatch = palette.getVibrantSwatch();
        Log.e("COLOR", swatch.getRgb()+"\n"+swatch.getHsl()+"\n"+swatch.getPopulation()+"\n"+swatch.getTitleTextColor()+"\n"+swatch.getBodyTextColor());
		
        textView1.setText(swatch.getRgb()+"\n"+swatch.getHsl()+"\n"+swatch.getPopulation()+"\n"+swatch.getTitleTextColor()+"\n"+swatch.getBodyTextColor());*/       
	}

	
}
