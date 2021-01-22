package com.inpt.lsb;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    private Animation bottomAnim,topAnim;
    private ImageView logo;
    private TextView slogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        logo=findViewById(R.id.logo);
        slogan=findViewById(R.id.slogan);
        animation();
        Pair[] pairs=new Pair[2];
        pairs[0]=new Pair<View,String>(logo,"logo_transition");
        pairs[1]=new Pair<View,String>(slogan,"slogan_transition");
        new Handler().postDelayed(()->{
            Intent intent=new Intent(this,SignInActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(this,pairs);
                startActivity(intent,activityOptions.toBundle());
            }
        },2000);
    }

    private void animation() {
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_anim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
    }



}