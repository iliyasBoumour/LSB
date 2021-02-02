package com.inpt.lsb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.UploadImage;
import com.inpt.models.Post;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView,add_btn,cancelAdd;
    private EditText caption_field;
    private Uri imageUri;
    private String currentUserId;
    private UploadImage uploadImage;
    private ProgressDialog progressDialog;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        imageView = findViewById(R.id.imageView);
        add_btn = findViewById(R.id.add_btn);
        caption_field = findViewById(R.id.caption_field);
        cancelAdd=findViewById(R.id.cancelAdd);
        cancelAdd.setOnClickListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.please_wait));
        uploadImage=new UploadImage(this);
        imageUri = getIntent().getParcelableExtra("imageUri");
        imageView.setImageURI(imageUri);
        add_btn.setOnClickListener(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                addPost();
                break;
            case R.id.cancelAdd:
                finish();
                break;
        }
    }

    private void addPost() {
        String caption = caption_field.getText().toString().trim();
        progressDialog.show();
        if(imageUri != null) {
            StorageReference filepath = storageReference
                    .child("post_images")
                    .child(currentUserId + Timestamp.now().getSeconds());
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Post post = new Post();
                                    if (caption.isEmpty())  post.setCaption("");
                                    else post.setCaption(caption);
                                    post.setImageUrl(uri.toString());
                                    post.setNbLike(0);
                                    post.setUserId(currentUserId);
                                    post.setTimeAdded(new Timestamp(new Date()));
                                    post.setPostId(currentUserId + Timestamp.now().getSeconds());
                                    db.collection("Posts")
                                            .add(post)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(e->{
                                                progressDialog.dismiss();
                                                showDialog();
                                            });
                                }
                            });
                        }
                    });
        } else {
            progressDialog.dismiss();
            showDialog();
        }

    }

    private void showDialog(){
        new AlertDialog.Builder(this).setMessage("error uploading image please try later !").setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }).show();
    }
}