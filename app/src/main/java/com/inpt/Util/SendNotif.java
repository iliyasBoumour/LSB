package com.inpt.Util;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inpt.models.NotificationModel;
import com.inpt.notifications.APIService;
import com.inpt.notifications.Client;
import com.inpt.notifications.Data;
import com.inpt.notifications.MyResponse;
import com.inpt.notifications.NotificationSender;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotif {
    private NotificationModel notificationModel;
    private String title,message;
    private APIService apiService;
    private static final String NOTIF_LIKE="like";
    private static final String NOTIF_FOLLOW="follow";
    private static final String NOTIF_MESSAGE="message";
    public SendNotif(NotificationModel notificationModel){
        this.notificationModel=notificationModel;
        switch (notificationModel.getType()){
            case NOTIF_FOLLOW :
                this.title="New Follow";
                this.message=notificationModel.getFromName()+" starts following you";
                break;
            case NOTIF_LIKE :
                this.title="New Like";
                this.message=notificationModel.getFromName()+" likes your post";
                break;
            case NOTIF_MESSAGE :
                this.title=notificationModel.getFromName();
                this.message=notificationModel.getMessage();
                break;
        }
        this.apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }
    public void send(){
        FirebaseFirestore.getInstance().collection("Tokens").document(notificationModel.getTo()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String usertoken = documentSnapshot.getString("token");
                        sendNotifications(usertoken,notificationModel);
                    }
                });
    }

    public void sendNotifications(String usertoken, NotificationModel notificationModel) {
//        (String userId, String userName, String type, String postId, String pdpUrl)
        Data data;
        if (!notificationModel.getType().equals(NOTIF_MESSAGE)) {
            data = new Data(notificationModel.getFrom(), notificationModel.getFromName(), notificationModel.getType()
                    , notificationModel.getImageNotified(), notificationModel.getFromPdp(), notificationModel.getPostId()
                    , notificationModel.getToUsername(), notificationModel.getToPdp(), notificationModel.getTo());
        }else{
            data = new Data(notificationModel.getFrom(), notificationModel.getFromName(),notificationModel.getFromPdp(),notificationModel.getMessage(),notificationModel.getType(),notificationModel.getTo());
        }
        Log.d("TAG", "sendNotifications: "+notificationModel.getFromPdp());
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    Log.d("OOOOOOnResponse",response.message());
                    if (response.body().success != 1) {
//                        Toast.makeText(MainActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
}
