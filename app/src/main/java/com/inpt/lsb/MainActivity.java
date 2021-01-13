package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
private Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null) {
            startActivity(new Intent(this,LandingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        logout=findViewById(R.id.button);
        logout.setOnClickListener(E->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,SignInActivity.class));
            finish();
        });
    }
}