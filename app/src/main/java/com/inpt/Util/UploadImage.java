package com.inpt.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.inpt.lsb.R;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayOutputStream;

public class UploadImage  {

    private StorageReference mStorageRef;
    private StorageTask uploadTask;
    private AppCompatActivity appCompatActivity;
    private static final int REQUEST_CODE = 1;
    private PickImageDialog dialog ;
    private String currentUserId = CurrentUserInfo.getInstance().getUserId();

    public UploadImage(String location,AppCompatActivity appCompatActivity){
        this.mStorageRef = FirebaseStorage.getInstance().getReference().child(location);
        this.appCompatActivity=appCompatActivity;
    }

    public void verifyPermissions(){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(appCompatActivity.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(appCompatActivity.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(appCompatActivity.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }else{
            ActivityCompat.requestPermissions(appCompatActivity,
                    permissions,
                    REQUEST_CODE);
        }
    }

    public void selectImage() {
        try {
            @SuppressLint("WrongConstant")
            PickSetup setup = new PickSetup()
                    .setButtonOrientation(LinearLayoutCompat.HORIZONTAL)
                    .setCameraButtonText(appCompatActivity.getString(R.string.Camera))
                    .setGalleryButtonText(appCompatActivity.getString(R.string.Gallery));
            dialog=PickImageDialog.build(setup)
                    .setOnClick(new IPickClick() {
                        @Override
                        public void onGalleryClick() {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            appCompatActivity.startActivityForResult(pickPhoto, 1);
                        }

                        @Override
                        public void onCameraClick() {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            appCompatActivity.startActivityForResult(intent, 0);
                        }
                    }).show(appCompatActivity);
//
        } catch (Exception e) {
            Toast.makeText(appCompatActivity, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = appCompatActivity.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public String uploadImage(Uri imageUri){
        final String[] url={""};
        if (imageUri != null) {
            StorageReference fileReference = mStorageRef.child(currentUserId +System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            fileReference.getDownloadUrl().addOnSuccessListener(uri->{
                                url[0]= uri.toString();
                                Log.i("eeeeeeeeeUp",url[0]);
                            });
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            url[0]="";
                            Log.i("eeeeeeeeeNoUp","failed"+url[0]);
                            Toast.makeText(appCompatActivity, "failed to upload image please try later", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return url[0];
    }
}
