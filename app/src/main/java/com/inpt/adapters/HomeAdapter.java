package com.inpt.adapters;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.inpt.lsb.PostFragment;
import com.inpt.lsb.ProfileCurrentUserFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Likes");
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private CollectionReference collectionReferencePost = db.collection("Posts");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");

    private FragmentManager fragmentManager;
    private static final float SWIPE_THRESHOLD = 50;
    private static final float SWIPE_VELOCITY_THRESHOLD = 50;


    private String currentUserId = CurrentUserInfo.getInstance().getUserId();


    public HomeAdapter(Context context, List<Post> posts, FragmentManager fragmentManager) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                return o2.getTimeAdded().compareTo(o1.getTimeAdded());
            }
        });
        this.context = context;
        this.posts = posts;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);

        return new ViewHolder(view, context, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        Uri uri = Uri.parse(post.getImageUrl());
        String time = (String) DateUtils.getRelativeTimeSpanString(post.getTimeAdded().getSeconds() * 1000);
        String caption = post.getCaption();
        String postId = post.getPostId();
        String userId = post.getUserId();
        int likeNb = post.getNbLike();
        holder.postImage.setImageURI(uri);
        holder.timeTextView.setText(time);
        holder.captionTextView.setText(caption);
        holder.checkReact(currentUserId, postId);
        holder.setUserInfo(userId);
        holder.likeNb.setText(setnbLike(likeNb));


    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String setnbLike(int nb) {
        String str = "";
        if (nb > 1) {
            str = nb + "Likes";
        } else if (nb == 1) {
            str = nb + "Like";
        }
        return str;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public SimpleDraweeView postImage, pdpImage;
        public TextView timeTextView, captionTextView, userNameTextView, likeNb;
        public ImageView like_icone;
        public Boolean isliked;
        public int nbLike;
        String userName;
        String pdpUrl;
        FragmentManager fragmentManager;
        Fragment fragment;


        public void checkReact(String currentUserId, String postId) {
            collectionReference.document(currentUserId + "_" + postId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                like_icone.setImageResource(R.drawable.ic_after);
                                isliked = true;
                            } else {
                                like_icone.setImageResource(R.drawable.ic_before);
                                isliked = false;
                            }
                        }
                    });
        }

        public void setUserInfo(String userId) {
            collectionReferenceUsers.whereEqualTo("uid", userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                    userName = userDocument.getString("username");
                                    pdpUrl = userDocument.getString("pdp");
                                    Uri uri = Uri.parse(pdpUrl);
                                    pdpImage.setImageURI(uri);
                                    userNameTextView.setText(userName);
                                }
                            }
                        }
                    });
        }

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull final View itemView, final Context ctx, final FragmentManager f) {
            super(itemView);
            context = ctx;
            fragmentManager = f;

            postImage = itemView.findViewById(R.id.postImage);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            captionTextView = itemView.findViewById(R.id.captionTextView);
            like_icone = itemView.findViewById(R.id.likeImageView);
            pdpImage = itemView.findViewById(R.id.pdpImageView);
            userNameTextView = itemView.findViewById(R.id.userName_textView);
            likeNb = itemView.findViewById(R.id.likes_nb);
            like_icone.setOnClickListener(this);
            pdpImage.setOnClickListener(this);
            userNameTextView.setOnClickListener(this);
            postImage.setOnClickListener(new DoubleClickListener() {
                @Override
                public void onDoubleClick() {
                    React(posts.get(getAdapterPosition()).getPostId(), posts.get(getAdapterPosition()).getUserId() );
                }
            });
        }

        private void React(String postId, String userId) {
            if (isliked) {
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
                                                                                    likeNb.setText(setnbLike(nbLike));
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
                                                                                    likeNb.setText(setnbLike(nbLike));
                                                                                    //Notification
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


        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.likeImageView) {
                React(posts.get(getAdapterPosition()).getPostId(), posts.get(getAdapterPosition()).getUserId());
            } else if (v.getId() == R.id.pdpImageView || v.getId() == R.id.userName_textView) {
                String userId = posts.get(getAdapterPosition()).getUserId();
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userName", userName);
                bundle.putString("pdpUrl", pdpUrl);
                fragment = fragmentManager.findFragmentById(R.id.homeFragment);
                if (userId.contentEquals(currentUserId)) {
                    fragment = new ProfileCurrentUserFragment();
                } else {
                    fragment = new ProfileOtherUsersFragment();
                    fragment.setArguments(bundle);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .commit();
            }
        }


    }
}
