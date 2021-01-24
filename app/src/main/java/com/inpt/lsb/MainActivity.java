package com.inpt.lsb;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.inpt.Util.SendNotif;
import com.inpt.notifications.Token;

public class MainActivity extends AppCompatActivity {
    private Button logout;
//    private APIService apiService;
    EditText UserTB;
    SendNotif sendNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout=findViewById(R.id.button);
        UserTB=findViewById(R.id.UserTB);
//        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        logout.setOnClickListener(v -> {

//            sendNotif=new SendNotif("iliyas",UserTB.getText().toString().trim(),"like");
//            sendNotif.send();
                });
        UpdateToken();
    }
    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseFirestore.getInstance().collection("Tokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(token);
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