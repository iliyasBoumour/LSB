package com.inpt.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.inpt.lsb.R;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayOutputStream;

public class UploadImage  {

    private AppCompatActivity appCompatActivity;
    private static final int REQUEST_CODE = 1;
    private PickImageDialog dialog ;
    private static int i=0;

    public UploadImage(AppCompatActivity appCompatActivity){
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
            Toast.makeText(appCompatActivity, "Permissions error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
