package com.inpt.lsb;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1 ;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    FragmentManager fragmentManager;
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        floatingActionButton = findViewById(R.id.add_post_btn);
        floatingActionButton.setOnClickListener(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null) {
            startActivity(new Intent(this,LandingActivity.class));
            finish();
            return;
        }
        bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                fragment = new HomeFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .commit();

                return false;
            }
        });
        bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                fragment = new ProfileCurrentUserFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .commit();

                return false;
            }
        });

        fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.homeFragment);
        if(fragment == null) {
            fragment = new ProfileCurrentUserFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.homeFragment, fragment)
                    .commit();
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.add_post_btn:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri imageUri = data.getData();
                Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
                intent.putExtra("imageUri", imageUri);
                startActivity(intent);
            }
        }
    }
}