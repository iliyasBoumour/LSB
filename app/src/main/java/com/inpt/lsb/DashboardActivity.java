package com.inpt.lsb;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;
import com.inpt.notifications.Token;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    public static BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Fragment fragment;
    private UploadImage uploadImage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private String tagFragment;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.black));

        setContentView(R.layout.dashboard);
        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottomNavView);
        fragment = fragmentManager.findFragmentById(R.id.homeFragment);
        uploadImage=new UploadImage(this);
        UpdateToken();
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        String menuFragment = getIntent().getStringExtra("Fragment");
        if(menuFragment != null) {
            Bundle bundle = new Bundle();
            Log.d("ID", "onCreate: " + getIntent().getStringExtra("postId"));
            Log.d("ID", "onCreate: " + menuFragment);

            switch (menuFragment) {
                case "profileOtherUsers":
                    fragment = new ProfileOtherUsersFragment();
                    bundle.putString("userName", getIntent().getStringExtra("userName"));
                    bundle.putString("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                    bundle.putString("userId", getIntent().getStringExtra("userId"));
                    fragment.setArguments(bundle);

                    db.collection("users").document(getIntent().getStringExtra("userId")).get().addOnSuccessListener(
                            documentSnapshot -> {
                                if (!documentSnapshot.exists()){

                                }
                            }
                    );
                    break;
                case "post":
                    fragment = new PostFragment();
                    bundle.putString("postId", getIntent().getStringExtra("postId"));
                    bundle.putString("userName", getIntent().getStringExtra("userName"));
                    bundle.putString("pdpUrl", getIntent().getStringExtra("pdpUrl"));
                    bundle.putString("userId", getIntent().getStringExtra("userId"));
                    bundle.putString("From", "Dashboard");
                    fragment.setArguments(bundle);
                    break;
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.homeFragment, fragment)
                    .commit();

        } else if(fragment == null) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.homeFragment, fragment)
                    .commit();
        }

        Log.d("LAST LINE", "onCreate: ");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    private void setStatus(String status) {
        collectionReferenceUsers.whereEqualTo("uid", currentUserInfo.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", status);
                        for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                            Log.d("STATUS", "onSuccess: ");
                            userDocument.getReference().update(data);
                        }
                    }
                });
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
       /* switch(v.getId()) {
            case R.id.add_post_btn:
                uploadImage.verifyPermissions();
                break;
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale( permission );
                if (! showRationale) {
                    Toast.makeText(this, getString(R.string.permission_settings), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
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

    @Override
    public void onBackPressed() {
       /* if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            getSupportFragmentManager().popBackStack();
            finish();

        }*/
        super.onBackPressed();
    }

    // MENU
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if(item.getItemId() == R.id.menuAdd) {
            uploadImage.verifyPermissions();
        } else {
            switch(item.getItemId()) {
                case R.id.menuHome:
                    fragment = new HomeFragment();
                    tagFragment = "home";
                    break;
                case R.id.menuProfil:
                    fragment = new ProfileCurrentUserFragment();
                    tagFragment = "profile";
                    break;
                case R.id.menuNotification:
                    fragment = new NotificationsFragment();
                    tagFragment = "notification";
                    break;
                case R.id.menuSearch:
                    fragment = new SearchFragment();
                    tagFragment = "search";
            }
            Fragment topFragment = getSupportFragmentManager().findFragmentByTag(tagFragment);
            if( topFragment == null ) {
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment, tagFragment)
                        .addToBackStack(null)
                        .commit();
            }else {
                fragmentManager.popBackStack();
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment, tagFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }





        return false;
    }

}