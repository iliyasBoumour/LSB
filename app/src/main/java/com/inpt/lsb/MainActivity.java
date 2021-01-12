package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
private Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout=findViewById(R.id.button);
        logout.setOnClickListener(E->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,SignInActivity.class));
            finish();
        });
    }
}