package com.inpt.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.NotificationModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    NotificationModel notificationModel = new NotificationModel();
    String message, title, CHANNEL_NAME, CHANNEL_ID;
    private static final String NOTIF_LIKE = "like";
    private static final String NOTIF_FOLLOW = "follow";
    private static int i = 1;
    private PendingIntent pendingIntent;
    String GROUP_KEY = "LSB";
    private CurrentUserInfo currentUserInfo= CurrentUserInfo.getInstance();
    Intent notificationIntent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        notificationModel.setType(remoteMessage.getData().get("type"));
        notificationModel.setFromName(remoteMessage.getData().get("userName"));

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
                Intent notificationIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                notificationIntent.putExtra("Fragment", "profileOtherUsers");
                notificationIntent.putExtra("pdpUrl", notificationModel.getFromPdp());
                notificationIntent.putExtra("userName", notificationModel.getFromName());
                Log.d("FOLLOW", "onMessageReceived: " + notificationModel.getFromName());
                notificationIntent.putExtra("userId", notificationModel.getFrom());
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 	PendingIntent.FLAG_UPDATE_CURRENT);
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

                //
                Intent postNotif = new Intent(getApplicationContext(), DashboardActivity.class);
                postNotif.putExtra("Fragment", "post");
                postNotif.putExtra("postId", notificationModel.getPostId());
                postNotif.putExtra("pdpUrl", notificationModel.getToPdp());
                postNotif.putExtra("userName", notificationModel.getToUsername());
                postNotif.putExtra("userId", notificationModel.getTo());
                Log.d("LIKE", "onMessageReceived: " + notificationModel.getToUsername());


                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, postNotif, 	PendingIntent.FLAG_UPDATE_CURRENT);
                break;
        }


        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIF_FOLLOW, NOTIF_FOLLOW,
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

//        TODO: round image

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
                .setStyle(new NotificationCompat.InboxStyle()
                .addLine(title + " " + message));
        manager.notify(i++, notification);
        manager.notify(0, summaryNotification.build());
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

}