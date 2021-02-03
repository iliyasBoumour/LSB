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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LandingActivity extends AppCompatActivity {

    private Animation bottomAnim, topAnim;
    private ImageView logo;
    private TextView slogan;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private Intent intent;
    private static int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadSettings();
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return;
            }, 2000);
        } else {
            String menuFragment = getIntent().getStringExtra("Fragment");
            if (menuFragment != null) {
                switch (menuFragment) {
                    case "profileOtherUsers":
                        intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra("userName", getIntent().getStringExtra("userName"));
                        intent.putExtra("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                        intent.putExtra("userId", getIntent().getStringExtra("userId"));
                        intent.putExtra("Fragment", "profileOtherUsers");
                        break;
                    case "post":
                        intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra("postId", getIntent().getStringExtra("postId"));
                        intent.putExtra("userName", getIntent().getStringExtra("userName"));
                        intent.putExtra("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                        intent.putExtra("userId", getIntent().getStringExtra("userId"));
                        intent.putExtra("Fragment", "post");
                        break;
                    case "chat":
                        Bundle bundle = getIntent().getExtras();
                        intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("userName", bundle.getString("userName"));
                        intent.putExtra("pdpUrl", bundle.getString("pdpUrl"));
                        intent.putExtra("userId", bundle.getString("userId"));
                        intent.putExtra("From", "Landing");
                        break;
                }
            }else{
                intent=new Intent(this,DashboardActivity.class);
            }
            if (currentUserInfo.getUserId()!=null) {
                startActivity(intent);
                finish();
                return;
            }
            String uid = currentUser.getUid();
            currentUserInfo.setUserId(uid);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            currentUserInfo.setUserName(doc.getString("username"));
                            currentUserInfo.setPdpUrl(doc.getString("pdp"));
                        }
                        startActivity(intent);
                        finishAffinity();
                    });
        }
        setContentView(R.layout.activity_landing);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.slogan);
        animation();


    }



    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        boolean isDark=preferences.getBoolean("dark",false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d("TAG", "dark: ");
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d("TAG", "light: ");
        }


        if (!preferences.getString("language", "we").equals("we")) {
            Locale locale = null;
            switch (preferences.getString("language", "English")) {
                case "Francais":
                    locale = new Locale("fr");
                    break;
                case "English":
                    locale = new Locale("en");
                    break;
            }
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.locale = locale;
            getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        } else {
            SharedPreferences.Editor editor = getSharedPreferences("Settings", Activity.MODE_PRIVATE).edit();
            if (Locale.getDefault().getLanguage().equals("fr")) {
                editor.putString("language", "Francais");
                editor.apply();
            }
        }
    }

    private void animation() {
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
    }


}