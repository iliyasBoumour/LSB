package com.inpt.lsb;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.DoubleClickListener;
import com.inpt.Util.SendNotif;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PostFragment extends Fragment implements View.OnClickListener{
    private SimpleDraweeView simpleDraweeView, pdpImage;
    private TextView captionTextView, timeTextView, likesTextView, userNameTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private ImageView like_icone, threeDotMenu;
    private String currentUserId;
    private String postId;
    private String postImageUrl;
    private Boolean isliked;
    private String userId;
    private int nbLike;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Likes");
    private CollectionReference collectionReferencePost = db.collection("Posts");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");

    public static final String TAG = "EVENT";
    private static final String NOTIF_LIKE="like";
    SendNotif sendNotif;
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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        captionTextView = view.findViewById(R.id.caption);
        likesTextView = view.findViewById(R.id.likes_nb);
        like_icone = view.findViewById(R.id.like);
        timeTextView = view.findViewById(R.id.time);
        threeDotMenu = view.findViewById(R.id.threeDotMenu);
        if(!currentUserId.contentEquals(userId)) {
            threeDotMenu.setVisibility(View.INVISIBLE);
        }
        threeDotMenu.setOnClickListener(this);
        pdpImage = view.findViewById(R.id.pdp_imageView);
        userNameTextView = view.findViewById(R.id.userName_textView);
        like_icone.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPost(postId);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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
            case R.id.threeDotMenu:
                PopupMenu popupMenu = new PopupMenu(Objects.requireNonNull(getActivity()).getApplicationContext(), threeDotMenu);
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                Toast.makeText(getActivity(), "DELETE", Toast.LENGTH_LONG).show();
                                break;
                            case R.id.edit:
                                Toast.makeText(getActivity(), "EDIT", Toast.LENGTH_LONG).show();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

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
                                                                                collectionReferenceNotif.document(currentUserId + "_" + NOTIF_LIKE + "_" + postId).delete();
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
                                                                                notificationModel.setType(NOTIF_LIKE);
                                                                                notificationModel.setPostId(postId);
                                                                                notificationModel.setDate(new Timestamp(new Date()));
                                                                                if(!currentUserId.contentEquals(userId)) {
                                                                                    collectionReferenceNotif.document(currentUserId + "_" + NOTIF_LIKE + "_" + postId).set(notificationModel)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    sendNotif=new SendNotif(CurrentUserInfo.getInstance().getUserName(),userId,NOTIF_LIKE);
                                                                                                    sendNotif.send();
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
                        postImageUrl = post.getImageUrl();
                        int nbLike = post.getNbLike();
                        String time = (String) DateUtils.getRelativeTimeSpanString(post.getTimeAdded().getSeconds() * 1000);
                        Uri uri = Uri.parse(postImageUrl);
                        captionTextView.setText(caption);
                        simpleDraweeView.setImageURI(uri);
                        likesTextView.setText(setnbLike(nbLike));
                        timeTextView.setText(time);
                    }
                });
    }

    private void deletePost(String postId) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(postImageUrl);
        photoRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        collectionReferencePost.whereEqualTo("postId", postId).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                                            postDocument.getReference().delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            collectionReference.whereEqualTo("postId", postId).get()
                                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                            for(QueryDocumentSnapshot likeDocument : queryDocumentSnapshots) {
                                                                                likeDocument.getReference().delete()
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                collectionReferenceNotif.whereEqualTo("postId", postId).get()
                                                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                                for(QueryDocumentSnapshot notifDocument : queryDocumentSnapshots) {
                                                                                                                    notifDocument.getReference().delete()
                                                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onSuccess(Void aVoid) {

                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });

    }



}
