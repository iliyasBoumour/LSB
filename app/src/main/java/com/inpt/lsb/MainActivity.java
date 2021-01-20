package com.inpt.lsb;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.inpt.notifications.APIService;
import com.inpt.notifications.Client;
import com.inpt.notifications.Data;
import com.inpt.notifications.MyResponse;
import com.inpt.notifications.NotificationSender;
import com.inpt.notifications.Token;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private Button logout;
    private APIService apiService;
    EditText UserTB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout=findViewById(R.id.button);
        UserTB=findViewById(R.id.UserTB);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        logout.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance().collection("Tokens").document(UserTB.getText().toString().trim()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String usertoken = documentSnapshot.getString("token");
                                    Log.d("OOOOOOTOKEN", "onSuccess: "+usertoken);
                                    sendNotifications(usertoken, "ha titre", "ha lmessage");
                                }
                            });
                });
        UpdateToken();
    }
    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseFirestore.getInstance().collection("Tokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(token);
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
                        Toast.makeText(MainActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
}

//    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser==null) {
//                startActivity(new Intent(this,LandingActivity.class));
//        finish();
//        return;
//        }
//        setContentView(R.layout.activity_main);
//        logout=findViewById(R.id.button);
//        logout.setOnClickListener(E->{
//        FirebaseAuth.getInstance().signOut();
//        startActivity(new Intent(this,SignInActivity.class));
//        finish();
//        });
//        }