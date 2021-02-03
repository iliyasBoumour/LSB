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
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
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
    String message, title;
    private static final String NOTIF_LIKE = "like";
    private static final String NOTIF_FOLLOW = "follow";
    private static final String NOTIF_MESSAGE = "message";
    private static String TAG = "TAG";
    private PendingIntent pendingIntent;
    private String GROUP_KEY = "LSB";
    private NotificationManagerCompat manager;
    private String channelParams = "LSB";
    private Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private Bitmap image = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences preferences = getSharedPreferences("NOTIF", MODE_PRIVATE);
        String userId_ = preferences.getString("userId", "");

        manager= NotificationManagerCompat.from(getApplicationContext());

        notificationModel.setType(remoteMessage.getData().get("type"));
        notificationModel.setFromName(remoteMessage.getData().get("userName"));
        Long k = 0l;

        switch (notificationModel.getType()) {
            case NOTIF_FOLLOW:
                notificationModel.setFromPdp(remoteMessage.getData().get("pdpUrl"));
                notificationModel.setFrom(remoteMessage.getData().get("userId"));

                k = Long.parseLong(notificationModel.getFrom().replaceAll("[\\D]", ""));
                Log.d(TAG, "follow: " + k);

                title = getString(R.string.new_follow);
                message = notificationModel.getFromName() + " " + getString(R.string.starts_follow);
                image = getBitmapFromURL(notificationModel.getFromPdp());

                Intent notificationIntent = new Intent(getApplicationContext(), LandingActivity.class);
                notificationIntent.putExtra("Fragment", "profileOtherUsers");
                notificationIntent.putExtra("pdpUrl", notificationModel.getFromPdp());
                notificationIntent.putExtra("userName", notificationModel.getFromName());
                notificationIntent.putExtra("userId", notificationModel.getFrom());
                int j = toInt(k);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case NOTIF_LIKE:

                notificationModel.setPostUrl(remoteMessage.getData().get("postUrl"));
                notificationModel.setPostId(remoteMessage.getData().get("postId"));
                notificationModel.setToUsername(remoteMessage.getData().get("toUsername"));
                notificationModel.setToPdp(remoteMessage.getData().get("toPdp"));
                notificationModel.setTo(remoteMessage.getData().get("to"));

                k = Long.parseLong(notificationModel.getPostId().replaceAll("[\\D]", ""));
                Log.d(TAG, "like: " + k);

                title = getString(R.string.new_like);
                message = notificationModel.getFromName() + " " + getString(R.string.like_post);
                image = getBitmapFromURL(notificationModel.getPostUrl());

                Intent postNotif = new Intent(getApplicationContext(), LandingActivity.class);
                postNotif.putExtra("Fragment", "post");
                postNotif.putExtra("postId", notificationModel.getPostId());
                postNotif.putExtra("pdpUrl", notificationModel.getToPdp());
                postNotif.putExtra("userName", notificationModel.getToUsername());
                postNotif.putExtra("userId", notificationModel.getTo());
                Log.d("LIKE", "onMessageReceived: " + notificationModel.getToUsername());
                j = toInt(k);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j, postNotif, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case NOTIF_MESSAGE:
                notificationModel.setFromPdp(remoteMessage.getData().get("pdpUrl"));
                notificationModel.setMessage(remoteMessage.getData().get("message"));
                notificationModel.setFrom(remoteMessage.getData().get("userId"));
                k = Long.parseLong((notificationModel.getFrom() + "67").replaceAll("[\\D]", ""));
                j = toInt(k);
                Log.d(TAG, "msg: " + k);
                title = notificationModel.getFromName();
                message = notificationModel.getMessage();
                image = getBitmapFromURL(notificationModel.getFromPdp());
//                intent
                Intent chatIntent = new Intent(getApplicationContext(), LandingActivity.class);
                chatIntent.putExtra("Fragment", "chat");
                chatIntent.putExtra("userId", notificationModel.getFrom());
                chatIntent.putExtra("pdpUrl", notificationModel.getFromPdp());
                chatIntent.putExtra("userName", notificationModel.getFromName());
                chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), j, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (notificationModel.getType()) {
                case NOTIF_FOLLOW:
                    channelParams = NOTIF_FOLLOW;
                    break;
                case NOTIF_LIKE:
                    channelParams = NOTIF_LIKE;
                    break;
                case NOTIF_MESSAGE:
                    channelParams = NOTIF_MESSAGE;
                    break;
            }
            NotificationChannel channel = new NotificationChannel(channelParams, channelParams,
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

        }

        try {
            image = getCircleBitmap(image);
        } catch (Exception e) {
            Log.d("TAG", "onMessageReceived: error pdp" + e.getMessage());
        }
        if(notificationModel.getType().contentEquals(NOTIF_MESSAGE)) { //add condition
            if(!notificationModel.getFrom().contentEquals(userId_)) {
                showNotifMessage(pendingIntent, toInt(k));
            }
            return;
        }

        Notification notification =
                new NotificationCompat.Builder(getApplicationContext(), channelParams)
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


        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(this, channelParams)
                .setSmallIcon(R.drawable.logo)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat
                        .InboxStyle()
                        .addLine(title + " " + message)
                );
        manager.notify(toInt(k), notification);
        manager.notify(0, summaryNotification.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int toInt(Long k) {
        if (k > Integer.MAX_VALUE) {
            return (int) (k % 100000);
        }
        int k1 = Math.toIntExact(k);
        return k1;
    }

    private void showNotifMessage(PendingIntent pendingIntent, int i) {
        Notification notificationMessage = new NotificationCompat.Builder(getApplicationContext(), channelParams)
                .setContentTitle("New Message")
                .setContentText(title + " : " + message)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(image)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setSound(sound)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup("Messages")
                .build();
        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(this, channelParams)
                .setSmallIcon(R.drawable.logo)
                .setGroup("Messages")
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat
                        .InboxStyle()
                        .addLine(title + " : " + message)
                );

        manager.notify(i, notificationMessage);
        manager.notify(1, summaryNotification.build());
    }

    private Bitmap getBitmapFromURL(String strURL) {
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

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
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