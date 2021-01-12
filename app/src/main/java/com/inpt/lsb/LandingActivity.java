package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LandingActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUp,signIn;
    private Animation bottomAnim,topAnim;
    private ImageView logo;
    private TextView slogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        signIn=findViewById(R.id.signInButton);
        signUp=findViewById(R.id.signUpButton);
        logo=findViewById(R.id.logo);
        slogan=findViewById(R.id.slogan);
        signIn.setOnClickListener(this);
        signUp.setOnClickListener(this);
        animation();
    }

    private void animation() {
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_anim);
        signIn.setAnimation(bottomAnim);
        signUp.setAnimation(bottomAnim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(topAnim);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Pair[] pairs=new Pair[1];

        if (v.getId()==R.id.signInButton){
            intent = new Intent(this, SignInActivity.class);
            pairs[0]=new Pair<View,String>(signIn,"open");
        } else {
            intent = new Intent(this, SignUpActivity.class);
            pairs[0]=new Pair<View,String>(signUp,"open");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(this,pairs);
            startActivity(intent,options.toBundle());
        }else{
            startActivity(intent);
        }
    }
}