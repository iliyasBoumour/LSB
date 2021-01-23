package com.inpt.lsb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;
import com.inpt.notifications.Token;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    FragmentManager fragmentManager;
    Fragment fragment;
    private UploadImage uploadImage;
    private CurrentUserInfo currentUserInfo= CurrentUserInfo.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        bottomNavigationView = findViewById(R.id.bottomNavView);
        floatingActionButton = findViewById(R.id.add_post_btn);
        fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.homeFragment);
        uploadImage=new UploadImage(this);

        UpdateToken();

//        bottomNavigationView.setOnNavigationItemSelectedListener(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        floatingActionButton.setOnClickListener(this);

        if(fragment == null) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.homeFragment, fragment)
                    .commit();
        }
        Log.d("LAST LINE", "onCreate: ");
    }

    private void UpdateToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener( task-> {
                        if (!task.isSuccessful()) {
                            Log.d("ERROR",  "onComplete:  "  + task.getException().getMessage());
                            return;
                        }
                        String token_ = task.getResult();
                        Token token= new Token(token_);
                        FirebaseFirestore.getInstance().collection("Tokens").document(currentUserInfo.getUserId()).set(token);
                });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.add_post_btn:
                uploadImage.verifyPermissions();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        uploadImage.verifyPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri = null;
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    imageUri = uploadImage.getImageUri(getApplicationContext(), selectedImage);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    imageUri = data.getData();
                }
                break;
        }
        if (imageUri!=null){
            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
            intent.putExtra("imageUri", imageUri);
            startActivity(intent);
        }
    }

    // MENU
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuHome:
                fragment = new HomeFragment();
                break;
            case R.id.menuProfil:
                fragment = new ProfileCurrentUserFragment();
            break;
            case R.id.menuNotification:
                fragment = new NotificationsFragment();
                break;
            case R.id.menuSearch:
                fragment = new SearchFragment();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.homeFragment, fragment)
                .commit();
        return false;
    }

}