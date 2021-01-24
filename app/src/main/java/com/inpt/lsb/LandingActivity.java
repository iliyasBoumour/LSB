package com.inpt.lsb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.inpt.Util.CurrentUserInfo;

public class LandingActivity extends AppCompatActivity {

    private Animation bottomAnim,topAnim;
    private ImageView logo;
    private TextView slogan;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null) {
            new Handler().postDelayed(()->{
                startActivity(new Intent(this,SignInActivity.class));
                finish();
                return;
                },2000);
        } else {
            String uid=currentUser.getUid();
            currentUserInfo.setUserId(uid);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("uid",uid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots ->{
                        for(QueryDocumentSnapshot doc:queryDocumentSnapshots){
                            currentUserInfo.setUserName(doc.getString("username"));
                            currentUserInfo.setPdpUrl(doc.getString("pdp"));
                        }
                        startActivity(new Intent(this,DashboardActivity.class));
                        finish();
                    });

        }
        setContentView(R.layout.activity_landing);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        logo=findViewById(R.id.logo);
        slogan=findViewById(R.id.slogan);
        animation();

    }

    private void animation() {
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_anim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
    }



}