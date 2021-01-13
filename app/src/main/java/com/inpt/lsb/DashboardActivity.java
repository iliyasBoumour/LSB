package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null) {
            startActivity(new Intent(this,LandingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.dashboard);
        /*bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.setBackground(null);*/
        fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.homeFragment);
        if(fragment == null) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.homeFragment, fragment)
                    .commit();
        }

    }
}