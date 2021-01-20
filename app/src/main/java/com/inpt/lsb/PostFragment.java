package com.inpt.lsb;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.DoubleClickListener;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PostFragment extends Fragment implements View.OnClickListener{
    private SimpleDraweeView simpleDraweeView, pdpImage;
    private TextView captionTextView, timeTextView, likesTextView, userNameTextView;
    private ProgressBar progressBar;
    private ImageView like_icone;
    private String currentUserId;
    private String postId;
    private Boolean isliked;
    private String userId;
    private int nbLike;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Likes");
    private CollectionReference collectionReferencePost = db.collection("Posts");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");

    public static final String TAG = "EVENT";
    private Post post;


    public PostFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = this.getArguments().getString("postId");
        userId = this.getArguments().getString("userId");
        currentUserId = CurrentUserInfo.getInstance().getUserId();


    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_fragment, container, false);
        progressBar = view.findViewById(R.id.progressBar2);
        simpleDraweeView = view.findViewById(R.id.postImage);
        captionTextView = view.findViewById(R.id.caption);
        likesTextView = view.findViewById(R.id.likes_nb);
        like_icone = view.findViewById(R.id.like);
        timeTextView = view.findViewById(R.id.time);
        pdpImage = view.findViewById(R.id.pdp_imageView);
        userNameTextView = view.findViewById(R.id.userName_textView);
        like_icone.setOnClickListener(this);
        simpleDraweeView.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick() {
                React();
            }
        });
        Bundle bundle = this.getArguments();
        String userName = bundle.getString("userName");
        String pdpUrl = bundle.getString("pdpUrl");
        userNameTextView.setText(userName);
        Uri uriPdp = Uri.parse(pdpUrl);
        pdpImage.setImageURI(uriPdp);
        getPost(postId);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.like:
                React();
                break;
        }
    }

    private void React() {
        if(isliked) {
            like_icone.setEnabled(false);
            like_icone.setImageResource(R.drawable.ic_before);
            collectionReference.document(currentUserId + "_" + postId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            collectionReference.whereEqualTo("postId", postId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            nbLike = queryDocumentSnapshots.size();
                                            collectionReferencePost.whereEqualTo("postId", postId)
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                                                                Map<String, Object> data = new HashMap<>();
                                                                data.put("nbLike", nbLike);
                                                                postDocument.getReference().update(data)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                like_icone.setImageResource(R.drawable.ic_before);
                                                                                like_icone.setEnabled(true);
                                                                                isliked = false;
                                                                                likesTextView.setText(setnbLike(nbLike));
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

        } else {
            Map<String, String> data = new HashMap<>();
            data.put("userId", currentUserId);
            data.put("postId", postId);
            like_icone.setEnabled(false);
            like_icone.setImageResource(R.drawable.ic_after);
            collectionReference.document(currentUserId + "_" + postId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            collectionReference.whereEqualTo("postId", postId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            nbLike = queryDocumentSnapshots.size();
                                            collectionReferencePost.whereEqualTo("postId", postId)
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                                                                Map<String, Object> data = new HashMap<>();
                                                                data.put("nbLike", nbLike);
                                                                postDocument.getReference().update(data)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                like_icone.setImageResource(R.drawable.ic_after);
                                                                                like_icone.setEnabled(true);
                                                                                isliked = true;
                                                                                likesTextView.setText(setnbLike(nbLike));
                                                                                // Notification
                                                                                NotificationModel notificationModel = new NotificationModel();
                                                                                notificationModel.setFrom(currentUserId);
                                                                                notificationModel.setTo(userId);
                                                                                notificationModel.setType("like");
                                                                                notificationModel.setPostId(postId);
                                                                                notificationModel.setDate(new Timestamp(new Date()));
                                                                                if(!currentUserId.contentEquals(userId)) {
                                                                                    collectionReferenceNotif.add(notificationModel)
                                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                @Override
                                                                                                public void onSuccess(DocumentReference documentReference) {

                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                        }
                    });
        }


    }
    private void checkReact() {
        collectionReference.document(currentUserId + "_" + postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            like_icone.setImageResource(R.drawable.ic_after);
                            isliked = true;
                        } else {
                            like_icone.setImageResource(R.drawable.ic_before);
                            isliked = false;
                        }
                    }
                });
    }

    private String setnbLike(int nb) {
        String str = "";
        if(nb > 1) {
            str = nb + "Likes";
        } else if (nb == 1) {
            str = nb + "Like";
        }
        return str;
    }

    private void getPost(String postId) {
        progressBar.setVisibility(View.VISIBLE);
        collectionReferencePost.whereEqualTo("postId",postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        checkReact();
                        progressBar.setVisibility(View.INVISIBLE);
                        for(QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                            post = postDocument.toObject(Post.class);
                        }
                        String caption = post.getCaption();
                        String imageUrl = post.getImageUrl();
                        int nbLike = post.getNbLike();
                        String time = (String) DateUtils.getRelativeTimeSpanString(post.getTimeAdded().getSeconds() * 1000);
                        Uri uri = Uri.parse(imageUrl);
                        captionTextView.setText(caption);
                        simpleDraweeView.setImageURI(uri);
                        likesTextView.setText(setnbLike(nbLike));
                        timeTextView.setText(time);
                    }
                });
    }



}
