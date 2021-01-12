package com.inpt.lsb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LandingActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUp,signIn;
    private Animation bottomAnim,topAnim;
    private ImageView logo;
    private TextView slogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
            return;
        }
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

        if (v.getId()==R.id.signInButton){
            intent = new Intent(this, SignInActivity.class);
        } else {
            intent = new Intent(this, SignUpActivity.class);
        }
        startActivity(intent);
        finish();
    }

}