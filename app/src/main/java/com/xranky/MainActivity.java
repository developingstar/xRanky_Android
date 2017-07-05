package com.xranky;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.xranky.customView.CircleTransform;
import com.xranky.db.SQLiteFunction;
import com.xranky.db.SQliteHelp;
import com.xranky.model.AppState;
import com.xranky.model.Pref;
import com.xranky.model.User;

import java.util.Date;

public class MainActivity extends TabActivity {
    ImageView home_iv_popup,user_iv_view;
    TextView txtUserName,txtScrName,txtUpdated;

    TabHost tabHost;
    Pref pref;
    PopupWindow popup;
    SQLiteFunction db;
    public static String main = SQliteHelp.home;
    public static String pages = SQliteHelp.homepages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtScrName = (TextView) findViewById(R.id.txtScreen);
        txtUserName = (TextView) findViewById(R.id.txtUser);
        txtUpdated = (TextView) findViewById(R.id.txtUpdated);
        home_iv_popup = (ImageView) findViewById(R.id.home_iv_popup);
        user_iv_view = (ImageView) findViewById(R.id.ivUser);

        db = new SQLiteFunction(this, SQLiteFunction.WRITE);
        pref = new Pref(MainActivity.this);

        tabHost = getTabHost();
        setTabs();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setUpdatedTime();
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    setUserProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };



        Handler handler = new android.os.Handler();
        handler.postDelayed(runnable, 3000);

        home_iv_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                displayPopupWindow(v);
            }
        });
        pref = new Pref(MainActivity.this);
    }

    public void setTabs() {
        TabHost.TabSpec pagespec = tabHost.newTabSpec("Page");
        pagespec.setIndicator("",
                ResourcesCompat.getDrawable(getResources(), R.drawable.page_drawable, null));
        Intent pageIntent = new Intent(this,PagesActivity.class);
        pagespec.setContent(pageIntent);
        TabHost.TabSpec friendspec = tabHost.newTabSpec("Friend");
        friendspec.setIndicator("",
                ResourcesCompat.getDrawable(getResources(), R.drawable.friend_drawable, null));
        Intent friendIntent = new Intent(this, HomeActivity.class);
        friendspec.setContent(friendIntent);

        tabHost.addTab(friendspec);
        tabHost.addTab(pagespec);

        // Settingup Color of Tab : RED
        TabWidget widget = tabHost.getTabWidget();
        for (int i = 0; i < widget.getChildCount(); i++) {
            View v = widget.getChildAt(i);
            // Look for the title view to ensure this is an indicator and not a divider.
            TextView tv = (TextView) v.findViewById(android.R.id.title);
            if (tv == null) {
                continue;
            }
            v.setBackgroundResource(R.drawable.parth_tab_selector);
        }
    }

    public void setUserProfile(){
        User user = db.getAccount();
        if(!user.getProfileImage().isEmpty()) {
            Picasso.with(this).load(user.getProfileImage())
                    .error(R.drawable.user_default)
                    .placeholder(R.drawable.user_default)
                    .transform(new CircleTransform())
                    .into(user_iv_view);
        }

        txtUserName.setText(user.getName());
        txtScrName.setText("@" + user.getScrname());
    }

    public void setUpdatedTime(){
        AppState app = db.getAppState();
        Date date = new Date(app.updatedTime);
        int month = date.getMonth() + 1;
        int day = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes();
        txtUpdated.setText("Last update: " + month + "/" + day + " " + hour + ":" + min);
    }

    public void displayPopupWindow(View anchorView) {
        popup = new PopupWindow(getApplicationContext());
        View layout = getLayoutInflater().inflate(R.layout.popup_layout, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        final TextView tvContactus = (TextView)layout.findViewById(R.id.tvContactUs);
        final TextView tvLogout = (TextView)layout.findViewById(R.id.tvLogout);


        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvContactus.setBackgroundColor(0xdcdcdc);
                tvLogout.setBackgroundColor(0xFFB1AFAF);
                pref.edit();
                pref.editor.clear();
                pref.commit();
                CookieManager.getInstance().removeSessionCookie();
                db.logOut();
                new java.net.CookieManager().getCookieStore().removeAll();
                if(db.logOut()) {
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                }
            }
        });
        tvContactus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                tvLogout.setBackgroundColor(0xdcdcdc);
                tvContactus.setBackgroundColor(0xFFB1AFAF);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_SUBJECT, "xRanky Android app");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});

                try {
                    startActivity(Intent.createChooser(i, "Contact US"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(anchorView);
    }
}
