package com.inpt.lsb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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