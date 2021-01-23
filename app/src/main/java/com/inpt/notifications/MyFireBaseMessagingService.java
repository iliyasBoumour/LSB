package com.inpt.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.R;
import com.inpt.lsb.SignInActivity;
import com.inpt.models.NotificationModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    NotificationModel notificationModel = new NotificationModel();
    String message, title, CHANNEL_NAME, CHANNEL_ID;
    private static final String NOTIF_LIKE = "like";
    private static final String NOTIF_FOLLOW = "follow";
    private static int i=0;
    private PendingIntent pendingIntent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        type
        notificationModel.setType(remoteMessage.getData().get("type"));
//        username
        notificationModel.setFromName(remoteMessage.getData().get("userName"));
//        userID
        notificationModel.setFrom(remoteMessage.getData().get("userId"));
//        post id
        notificationModel.setPostId(remoteMessage.getData().get("postId"));
//        pdpUrl
        notificationModel.setFromPdp(remoteMessage.getData().get("pdpUrl"));
//        Log.d("OOOOOOOOOOOReceivd",notificationModel.getType()+" "+notificationModel.getFromName()+" "+notificationModel.getFrom()+" "+notificationModel.getFromPdp());
        Fresco.initialize(getApplicationContext());

//        PendingIntent contentIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        switch (notificationModel.getType()) {
            case NOTIF_FOLLOW:
                CHANNEL_ID = NOTIF_FOLLOW;
                CHANNEL_NAME = NOTIF_FOLLOW;
                message = getString(R.string.new_follow);
                title = notificationModel.getFromName() + getString(R.string.starts_follow);
                Intent followIntent=new Intent(getApplicationContext(), SignInActivity.class);
                pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,followIntent,0);
                break;
            case NOTIF_LIKE:
                CHANNEL_ID = NOTIF_LIKE;
                CHANNEL_NAME = NOTIF_LIKE;
                title = getString(R.string.new_like);
                message = notificationModel.getFromName() + getString(R.string.like_post);
                Intent LikeIntent=new Intent(getApplicationContext(), DashboardActivity.class);
                pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,LikeIntent,0);
                break;

        }

        Bitmap likedImage=getBitmapFromURL(notificationModel.getFromPdp());
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setLargeIcon(likedImage)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(sound)
                        .setOnlyAlertOnce(true)
//                        .setStyle(
//                                new NotificationCompat.BigPictureStyle().bigPicture(likedImage))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        manager.notify(i++, notification.build());
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