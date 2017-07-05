package com.xranky.customView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.util.AspectRatioImageView;
import com.xranky.R;
import com.xranky.db.SQLiteFunction;
import com.xranky.eventBus.AddToFriend;
import com.xranky.model.FeedItem;
import com.xranky.twitter.TwitterSupport;

import java.util.List;

import de.greenrobot.event.EventBus;

//import android.widget.ImageView;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {
 

	private List<FeedItem> feedItemList;
    private Context mContext;
    TwitterSupport twitter;
    SQLiteFunction db;

    public MyRecyclerAdapter(Context context, List<FeedItem> feedItemList, TwitterSupport twitter, SQLiteFunction db) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.twitter = twitter;
        this.db = db;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);

        //Download image using picasso library
        Picasso.with(mContext).load(feedItem.profilepic)
                .error(R.drawable.ic_launcher)
                .placeholder(R.drawable.ic_launcher)
                .transform(new CircleTransform())
                .into(customViewHolder.imageView);



        //Setting text view title
//        String vidURL = feedItem.videoURL;
//        Uri uri = Uri.parse(vidURL);
//        customViewHolder.videoView.setVideoURI(uri);

        String filePath = feedItem.videoURL; //change the location of your file!
//        Bitmap bmThumbnail;
//
////MICRO_KIND, size: 96 x 96 thumbnail
//        bmThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MICRO_KIND);
//        customViewHolder.imageview_micro.setImageBitmap(bmThumbnail);
//
//// MINI_KIND, size: 512 x 384 thumbnail
//        bmThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
//        customViewHolder.imageview_micro.setImageBitmap(bmThumbnail);

        if (!filePath.equals("null")) {
            Picasso.with(mContext).load(filePath)
                    .error(R.drawable.ic_launcher)
                    .placeholder(R.drawable.ic_launcher)
                    .transform(new RectTransform())
                    .into(customViewHolder.cImgView);
            customViewHolder.cImgView.setVisibility(View.VISIBLE);
        }else{

//            customViewHolder.cImgView.setVisibility(View.INVISIBLE);
        }


        if(feedItem.retweeted == 1){
            customViewHolder.reUserName.setText(Html.fromHtml("@" + feedItem.reUser + " Retweeted"));
        }
        customViewHolder.textView.setText(i + ":" +Html.fromHtml((feedItem.name == null)?"":feedItem.name));
        customViewHolder.tvText.setText(Html.fromHtml(feedItem.status));
        customViewHolder.tvfav.setText(""+feedItem.favorites);
        customViewHolder.tvReplay.setText(""+feedItem.replies);
        customViewHolder.tvRetwit.setText(""+feedItem.retweets);
        customViewHolder.tvScrName.setText("@"+feedItem.scrName);
        customViewHolder.tvTime.setText(""+feedItem.time);
        customViewHolder.llFooter.setTag(feedItem);
        
        if (feedItem.isFollow == 1) {
        	customViewHolder.row_iv_follow.setVisibility(View.INVISIBLE);
		}else {
			//customViewHolder.row_iv_follow.setVisibility(View.VISIBLE);
			customViewHolder.row_iv_follow.setTag(feedItem.userid);
			customViewHolder.row_iv_follow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
                        twitter.follow(v.getTag().toString());
                        notifyDataSetChanged();
                        EventBus.getDefault().post(new AddToFriend());
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
        
        customViewHolder.llFooter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FeedItem fi = (FeedItem) v.getTag();
				String url= "https://twitter.com/" + fi.scrName
					    + "/status/" +fi.id;
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				mContext.startActivity(i);
			}
		});

        customViewHolder.imageView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("onClick", "onClick");
//				PopupMenu popup = new PopupMenu(mContext, customViewHolder.imageView1);
//				MenuInflater inflater = popup.getMenuInflater();
//				inflater.inflate(R.menu.submenu, popup.getMenu());
//				popup.show();
//				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//					@Override
//					public boolean onMenuItemClick(MenuItem arg0) {
//						// TODO Auto-generated method stub
//						showdialog();
//						return false;
//					}
//				});
			}
		});
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }
    
    
    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        protected ImageView imageView,row_iv_follow;
        protected TextView textView,tvText,tvfav,tvRetwit,tvReplay,tvScrName,tvTime,reUserName;
        protected AspectRatioImageView cImgView;
        private VideoView videoView;
        ImageView imageView1;
        LinearLayout llFooter;

        public CustomViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.thumbnail);
//            videoView = (VideoView) view.findViewById(R.id.video_view);

            cImgView = (AspectRatioImageView)view.findViewById(R.id.cImgView);

            textView = (TextView) view.findViewById(R.id.title);
            reUserName = (TextView) view.findViewById(R.id.reUserName);
            imageView1 = (ImageView)view.findViewById(R.id.imageView1);
            tvText = (TextView) view.findViewById(R.id.feed_text);
            tvfav = (TextView) view.findViewById(R.id.tvfavourate);
            tvRetwit = (TextView) view.findViewById(R.id.tvRetwit);
            tvReplay = (TextView) view.findViewById(R.id.tvReplay);
            tvScrName = (TextView) view.findViewById(R.id.tvScrName);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
            row_iv_follow = (ImageView)view.findViewById(R.id.row_iv_follow);
            llFooter = (LinearLayout)view.findViewById(R.id.llFooter);

            imageView.setOnClickListener(this);
            imageView1.setOnClickListener(this);
            tvText.setOnClickListener(this);
            tvfav.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            FeedItem feedItem = feedItemList.get(position);
            Toast.makeText(mContext, feedItem.name, Toast.LENGTH_SHORT).show();

//            Intent i = new Intent(mContext, MainRecyclerDetails.class);
//            String getrec = feedItem.getTitle();
//            i.putExtra("title", getrec);
//            mContext.startActivity(i);
        }
    }
    
    private void showdialog() {
    	// TODO Auto-generated method stub
    	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Material Theme")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               })
               .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();
	}
}