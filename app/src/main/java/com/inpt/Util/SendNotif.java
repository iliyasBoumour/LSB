package com.inpt.Util;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inpt.notifications.APIService;
import com.inpt.notifications.Client;
import com.inpt.notifications.Data;
import com.inpt.notifications.MyResponse;
import com.inpt.notifications.NotificationSender;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotif {
    private String to;
    private String from;
    private String title;
    private String message;
    private APIService apiService;
    private static final String NOTIF_LIKE="like";
    private static final String NOTIF_FOLLOW="follow";
    public SendNotif(String from, String idTo,String type){
        this.to=idTo;
        switch (type){
            case NOTIF_FOLLOW :
                this.title="New Follow";
                this.message=from+" starts following you";
                break;
            case NOTIF_LIKE :
                this.title="New Like";
                this.message=from+" likes your post";
                break;
        }
        this.apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }
    public void send(){
        FirebaseFirestore.getInstance().collection("Tokens").document(this.to).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String usertoken = documentSnapshot.getString("token");
//                        Log.d("OOOOOOTOKEN", "onSuccess: "+usertoken);
                        sendNotifications(usertoken, title, message);
                    }
                });
    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        Log.d("OOOOOOnResponse","send notifs");
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
