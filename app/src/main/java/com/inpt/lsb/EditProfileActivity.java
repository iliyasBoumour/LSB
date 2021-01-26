package com.inpt.lsb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout textNameInput,textPasswordInput;
    private TextView editImage;
    private ImageView cancel,valid,pdp;
    private Button logout;
    SwitchMaterial darkMode;
    private UploadImage uploadImage;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private CurrentUserInfo currentUserInfo= CurrentUserInfo.getInstance();
    private boolean imageEdited=false;
    private FirebaseAuth mAuth;
    CollectionReference collection = FirebaseFirestore.getInstance().collection("users");
    private StorageReference storageReference;
    private boolean passwordUpdated=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        textNameInput=findViewById(R.id.textNameInput);
        textPasswordInput=findViewById(R.id.textPasswordInput);
        editImage=findViewById(R.id.editImage);
        pdp=findViewById(R.id.pdp);
        logout=findViewById(R.id.logout);
        cancel=findViewById(R.id.back);
        valid=findViewById(R.id.valid);
        darkMode=findViewById(R.id.darkMode);

        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.please_wait));

        pdp.setImageURI(Uri.parse(currentUserInfo.getPdpUrl()));

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        String strProvider = mAuth.getAccessToken(false).getResult().getSignInProvider();
        textNameInput.getEditText().setText(currentUserInfo.getUserName());
        if (strProvider.equals("google.com") || strProvider.equals("facebook.com")){
            textPasswordInput.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(this);
        valid.setOnClickListener(this);
        editImage.setOnClickListener(this);
        logout.setOnClickListener(this);
        darkMode.setOnCheckedChangeListener((compoundButton, b) -> editTheme(b));
        uploadImage=new UploadImage(this);
        textPasswordInput.getEditText().addTextChangedListener(createTextWatcher(textPasswordInput));
    }

    private void editTheme(boolean b) {
        if (b){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkMode.setChecked(true);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.valid:
                checkChanges();
                break;
            case R.id.editImage:
                uploadImage.verifyPermissions();
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this,SignInActivity.class));
                finishAffinity();
                break;
        }
    }

    private void checkChanges() {
        progressDialog.show();
        String newUserName =textNameInput.getEditText().getText().toString();
        if (currentUserInfo.getUserName()!=newUserName){
            collection.document(currentUserInfo.getUserId())
                    .update("username",newUserName);
            currentUserInfo.setUserName(newUserName);
            finish();
        }
//        if (passwordUpdated){
//        String newPassword =textPasswordInput.getEditText().getText().toString();
//            FirebaseUser user=mAuth.getCurrentUser();
//            AuthCredential credential = EmailAuthProvider
//                    .getCredential("user@example.com", "password1234");
//            user.reauthenticate(credential)
//                    .addOnCompleteListener(task -> {
//                        user.updatePassword(newPassword)
//                                .addOnCompleteListener(t -> {
//                                    if (t.isSuccessful()) {
//                                        Log.d("TAG", "checkChanges: ");
//                                        FirebaseAuth.getInstance().signOut();
//                                        startActivity(new Intent(this,SignInActivity.class));
//                                        finishAffinity();
//                                    }
//                                })
//                                .addOnFailureListener(e -> {
//                                    Log.d("TAG", "checkChanges: "+e.getMessage());
//                                });
//                    });
//        }
        if (imageEdited ){
            StorageReference filepath = storageReference
                    .child("pdps")
                    .child(currentUserInfo.getUserId() + Timestamp.now().getSeconds());
            filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                    currentUserInfo.setPdpUrl(uri.toString());
                    collection.document(currentUserInfo.getUserId())
                            .update("pdp",uri.toString());
                    progressDialog.dismiss();
                    finish();
                });
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        uploadImage.verifyPermissions();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    imageUri = uploadImage.getImageUri(getApplicationContext(), selectedImage);
                    pdp.setImageURI(imageUri);
                    imageEdited=true;
                }

                break;
            case 1:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    imageUri = data.getData();
                    pdp.setImageURI(imageUri);
                    imageEdited=true;
                }
                break;
        }

    }

    private TextWatcher createTextWatcher(TextInputLayout textInput) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordUpdated=true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}