package com.inpt.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.LandingActivity;
import com.inpt.lsb.R;
import com.inpt.models.NotificationModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    NotificationModel notificationModel = new NotificationModel();
    String message, title, CHANNEL_NAME, CHANNEL_ID;
    private static final String NOTIF_LIKE = "like";
    private static final String NOTIF_FOLLOW = "follow";
    private static final String NOTIF_MESSAGE="message";
    private static int i = 1;
    private static int j = 1;
    private PendingIntent pendingIntent;
    String GROUP_KEY = "LSB";
    private CurrentUserInfo currentUserInfo= CurrentUserInfo.getInstance();
    NotificationManagerCompat manager;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences preferences = getSharedPreferences("NOTIF", MODE_PRIVATE);
        Boolean send = preferences.getBoolean("send", true);


        notificationModel.setType(remoteMessage.getData().get("type"));
        notificationModel.setFromName(remoteMessage.getData().get("userName"));
        manager= NotificationManagerCompat.from(getApplicationContext());

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap image=null;
        switch (notificationModel.getType()) {
            case NOTIF_FOLLOW:
                notificationModel.setFromPdp(remoteMessage.getData().get("pdpUrl"));
                notificationModel.setFrom(remoteMessage.getData().get("userId"));

                CHANNEL_ID = NOTIF_FOLLOW;
                CHANNEL_NAME = NOTIF_FOLLOW;
                title = getString(R.string.new_follow);
                message= notificationModel.getFromName() + " " + getString(R.string.starts_follow);

                image = getBitmapFromURL(notificationModel.getFromPdp());

                //        notificationModel.getFromPdp(); notificationModel.getFromName();    notificationModel.getFrom();
                Intent notificationIntent;
//                if (currentUserInfo.getUserId()!=null) notificationIntent = new Intent(getApplicationContext(), DashboardActivity.class);
//                else
                    notificationIntent = new Intent(getApplicationContext(), LandingActivity.class);
                notificationIntent.putExtra("Fragment", "profileOtherUsers");
                notificationIntent.putExtra("pdpUrl", notificationModel.getFromPdp());
                notificationIntent.putExtra("userName", notificationModel.getFromName());
                notificationIntent.putExtra("userId", notificationModel.getFrom());
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j++, notificationIntent, 	PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case NOTIF_LIKE:

                notificationModel.setPostUrl(remoteMessage.getData().get("postUrl"));
                notificationModel.setPostId(remoteMessage.getData().get("postId"));
                notificationModel.setToUsername(remoteMessage.getData().get("toUsername"));
                notificationModel.setToPdp(remoteMessage.getData().get("toPdp"));
                notificationModel.setTo(remoteMessage.getData().get("to"));

//                Log.d("TAG", "onMessageReceived: "+notificationModel.getPostId()+" "+notificationModel.getToUsername()+" "+notificationModel.getTo()+" "+notificationModel.getToPdp());
                CHANNEL_ID = NOTIF_LIKE;
                CHANNEL_NAME = NOTIF_LIKE;
                title = getString(R.string.new_like);
                message = notificationModel.getFromName() + " " + getString(R.string.like_post);
                image = getBitmapFromURL(notificationModel.getPostUrl());

                Intent postNotif;
//                if (currentUserInfo.getUserId()!=null) postNotif = new Intent(getApplicationContext(), DashboardActivity.class);
//                else
                    postNotif = new Intent(getApplicationContext(), LandingActivity.class);
                postNotif.putExtra("Fragment", "post");
                postNotif.putExtra("postId", notificationModel.getPostId());
                postNotif.putExtra("pdpUrl", notificationModel.getToPdp());
                postNotif.putExtra("userName", notificationModel.getToUsername());
                postNotif.putExtra("userId", notificationModel.getTo());
                Log.d("LIKE", "onMessageReceived: " + notificationModel.getToUsername());


                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j++, postNotif, 	PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case NOTIF_MESSAGE:
                notificationModel.setFromPdp(remoteMessage.getData().get("pdpUrl"));
                notificationModel.setMessage(remoteMessage.getData().get("message"));
                notificationModel.setFrom(remoteMessage.getData().get("userId"));
                Log.d("TAG", "onMessageReceived: "+notificationModel.getFrom());
                title = notificationModel.getFromName();
                message=notificationModel.getMessage();
                image = getBitmapFromURL(notificationModel.getFromPdp());
//                intent
                Intent chatIntent;
//                if (currentUserInfo.getUserId()!=null) chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent = new Intent(getApplicationContext(), LandingActivity.class);
                chatIntent.putExtra("Fragment", "chat");
                chatIntent.putExtra("userId", notificationModel.getFrom());
                chatIntent.putExtra("pdpUrl", notificationModel.getFromPdp());
                chatIntent.putExtra("userName", notificationModel.getFromName());
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j++, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelParams;
//            switch (notificationModel.getType()){
//                case NOTIF_FOLLOW:
//                    channelParams=NOTIF_FOLLOW;
//                    break;
//                    case
//            }
            NotificationChannel channel = new NotificationChannel(NOTIF_FOLLOW, NOTIF_FOLLOW,
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

        }
        try {
            image=getCircleBitmap(image);
        }catch (Exception e){
            Log.d("TAG", "onMessageReceived: error pdp"+e.getMessage());
        }
        if(notificationModel.getType().contentEquals(NOTIF_MESSAGE) && send ) { //add condition
            Log.d("SEND", "onMessageReceived: " + send);
/*
            showNotifMessage(image,sound, pendingIntent);
*/
            return;
        }

        Notification notification =
                new NotificationCompat.Builder(getApplicationContext(), NOTIF_FOLLOW)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setLargeIcon(image)
                        .setSmallIcon(R.drawable.logo)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(sound)
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setGroup(GROUP_KEY).build();


        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(this, NOTIF_FOLLOW)
                .setSmallIcon(R.drawable.logo)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat
                        .InboxStyle()
                        .addLine(title + " " + message)
                );
        manager.notify(i++, notification);
        manager.notify(0, summaryNotification.build());
    }

    private void showNotifMessage(Bitmap image, Uri sound, PendingIntent pendingIntent) {
        Notification notification =
                new NotificationCompat.Builder(getApplicationContext(), NOTIF_FOLLOW)
                        .setContentTitle(title+": "+message)
//                        .setContentText(title+": "+message)
                        .setLargeIcon(image)
                        .setSmallIcon(R.drawable.logo)
                      .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(sound)
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH).build();
//                        .setGroup(GROUP_KEY).build();

        manager.notify(i++, notification);
    }

    Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }

}