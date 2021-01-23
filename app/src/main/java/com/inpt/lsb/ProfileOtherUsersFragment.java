package com.inpt.lsb;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.SendNotif;
import com.inpt.adapters.ProfileAdapter;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileOtherUsersFragment extends Fragment implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Post> posts;
    private String userId;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Posts");
    private CollectionReference collectionReferenceFollow = db.collection("Relations");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");
    private SimpleDraweeView pdp;
    private TextView userNameTextView;
    private String pdpUrl;
    private String userName;
    private MaterialButton followBtn;
    private Boolean follow;
    private static final String NOTIF_FOLLOW = "follow";
    SendNotif sendNotif;

    public ProfileOtherUsersFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        userId = this.getArguments().getString("userId");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_other_users_fragment, container, false);
        pdp = view.findViewById(R.id.pdp_imageView);
        followBtn = view.findViewById(R.id.follow_btn);
        followBtn.setOnClickListener(this);
        userNameTextView = view.findViewById(R.id.userName);
        recyclerView = view.findViewById(R.id.ProfilerecyclerView);
        Bundle bundle = this.getArguments();
        pdpUrl = bundle.getString("pdpUrl");
        Uri uri = Uri.parse(pdpUrl);
        userName = bundle.getString("userName");
        userNameTextView.setText(userName);
        pdp.setImageURI(uri);
        recyclerView = view.findViewById(R.id.ProfilerecyclerView);
        posts = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        checkFollow();
        getPosts();
        return view;
    }

    private void getPosts() {
        Log.d("CUI", "getPosts: " + userId);
        collectionReference.whereEqualTo("userId", userId)
                .orderBy("timeAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot post_ : queryDocumentSnapshots) {
                                Log.d("TEST", "onSuccess: ");

                                Post post = post_.toObject(Post.class);
                                Log.d("TEST", "onSuccess: " + post.getImageUrl());
                                posts.add(post);
                            }
                            if (getActivity() != null) {
                                Log.d("TEST2", "onSuccess: " + posts.size());
                                profileAdapter = new ProfileAdapter(getActivity(), posts, (getActivity()).getSupportFragmentManager(), userName, pdpUrl);
                                recyclerView.setAdapter(profileAdapter);
                                profileAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkFollow() {
        collectionReferenceFollow.document(currentUserId + "_" + userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            followBtn.setText("Unfollow");
                            follow = true;
                        } else {
                            followBtn.setText("Follow");
                            follow = false;
                        }
                    }
                });
    }

    private void followUser() {
        if (follow) {
            followBtn.setEnabled(false);
            collectionReferenceFollow.document(currentUserId + "_" + userId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            follow = false;
                            setFollowBtnText(follow);
                            followBtn.setEnabled(true);
                            collectionReferenceNotif.document(currentUserId + "_" + NOTIF_FOLLOW + "_" + userId).delete();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            followBtn.setEnabled(true);
                        }
                    });

        } else {
            followBtn.setEnabled(false);
            Map<String, String> data = new HashMap<>();
            data.put("userFollowerId", currentUserId);
            data.put("userFollowedId", userId);
            collectionReferenceFollow.document(currentUserId + "_" + userId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            follow = true;
                            setFollowBtnText(follow);
                            followBtn.setEnabled(true);
                            // Notification
//TODO: set the image uri
                            NotificationModel notificationModel = new NotificationModel();
                            notificationModel.setFrom(currentUserId);
                            notificationModel.setTo(userId);
                            notificationModel.setType(NOTIF_FOLLOW);
                            notificationModel.setPostId(null);
                            notificationModel.setFromName(CurrentUserInfo.getInstance().getUserName());
                            notificationModel.setFromPdp(CurrentUserInfo.getInstance().getPdpUrl());
                            notificationModel.setDate(new Timestamp(new Date()));
                            collectionReferenceNotif.document(currentUserId + "_" + NOTIF_FOLLOW + "_" + userId).set(notificationModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sendNotif = new SendNotif(notificationModel);
                                            sendNotif.send();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            followBtn.setEnabled(true);
                        }
                    });

        }
    }

    private void setFollowBtnText(Boolean follow) {
        if (follow) {
            followBtn.setText("Unfollow");
        } else {
            followBtn.setText("Follow");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.follow_btn:
                followUser();
                break;
        }
    }
}