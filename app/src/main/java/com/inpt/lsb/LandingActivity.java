package com.inpt.lsb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.inpt.Util.CurrentUserInfo;

import java.util.Locale;

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
            Intent intent = new Intent(this,DashboardActivity.class);
            String menuFragment = getIntent().getStringExtra("Fragment");
            if(menuFragment != null) {
                switch (menuFragment) {
                    case "profileOtherUsers":
                        intent.putExtra("userName", getIntent().getStringExtra("userName"));
                        intent.putExtra("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                        intent.putExtra("userId", getIntent().getStringExtra("userId"));
                        intent.putExtra("Fragment","profileOtherUsers");
                        break;
                    case "post":
                        intent.putExtra("postId", getIntent().getStringExtra("postId"));
                        intent.putExtra("userName", getIntent().getStringExtra("userName"));
                        intent.putExtra("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                        intent.putExtra("userId", getIntent().getStringExtra("userId"));
                        intent.putExtra("Fragment","post");
                        break;
                }
            }
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
                        startActivity(intent);
                        finish();
                    });

        }
        loadSettings();
        setContentView(R.layout.activity_landing);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        logo=findViewById(R.id.logo);
        slogan=findViewById(R.id.slogan);
        Log.d("TAG", "onCreate: ");
        animation();

    }

    private void loadSettings() {
        SharedPreferences preferences=getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        if (preferences.getBoolean("dark",false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        if (!preferences.getString("language","we").equals("we")){
            Log.d("TAG", "in preferences: "+Locale.getDefault().getLanguage());
            Locale locale = null;
            switch (preferences.getString("language","English")){
                case "Francais":
                    Log.d("TAG", "loadSettings: fr");
                    locale=new Locale("fr");
                    break;
                case "English":
                    Log.d("TAG", "loadSettings: en");
                    locale=new Locale("en");
                    break;
            }
            Locale.setDefault(locale);
            Configuration configuration=new Configuration();
            configuration.locale=locale;
            getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        }else {
            SharedPreferences.Editor editor=getSharedPreferences("Settings", Activity.MODE_PRIVATE).edit();
            if ( Locale.getDefault().getLanguage().equals("fr")){
                Log.d("TAG", "loadSettings: system laguage");
                editor.putString("language","Francais");
                editor.apply();
            }
        }
    }

    private void animation() {
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_anim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
    }



}