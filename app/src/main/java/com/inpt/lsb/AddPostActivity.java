package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.models.Post;

import java.util.Date;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private Button add_btn;
    private EditText caption_field;
    private Uri imageUri;
    private String currentUserId;
    private ProgressBar progressBar;

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Posts");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);
        imageView = findViewById(R.id.imageView);
        add_btn = findViewById(R.id.add_btn);
        caption_field = findViewById(R.id.caption_field);
        imageUri = getIntent().getParcelableExtra("imageUri");
        imageView.setImageURI(imageUri);
        add_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                addPost();
                break;
        }
    }

    private void addPost() {
        String caption = caption_field.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(caption) && imageUri != null) {
            StorageReference filepath = storageReference
                    .child("post_images")
                    .child(currentUserId + Timestamp.now().getSeconds());
            //Storage
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Post post = new Post();
                                    post.setCaption(caption);
                                    post.setImageUrl(imageUrl);
                                    post.setNbLike(0);
                                    post.setUserId(currentUserId);
                                    post.setTimeAdded(new Timestamp(new Date()));
                                    post.setPostId(currentUserId + Timestamp.now().getSeconds());

                                    //FireStore
                                    collectionReference.add(post)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            })      ;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });


        } else {

        }

    }
}