package com.inpt.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.DoubleClickListener;
import com.inpt.Util.SendNotif;
import com.inpt.lsb.ProfileCurrentUserFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.LikesModel;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;

import java.util.ArrayList;
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
    SendNotif sendNotif;


    private String currentUserId = CurrentUserInfo.getInstance().getUserId();
    private static final String NOTIF_LIKE = "like";

    private AlertDialog.Builder builder;
    private AlertDialog dialog;


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
        RecyclerView recyclerView;
        List<LikesModel> likes;
        LikesAdapter likesAdapter;


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
            likeNb.setOnClickListener(this);
            like_icone.setOnClickListener(this);
            pdpImage.setOnClickListener(this);
            userNameTextView.setOnClickListener(this);
            postImage.setOnClickListener(new DoubleClickListener() {
                @Override
                public void onDoubleClick() {
                    React(posts.get(getAdapterPosition()).getPostId(), posts.get(getAdapterPosition()).getUserId());
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
                                                                                    likeNb.setText(setnbLike(nbLike));
                                                                                    //Notification
//                                                                                TODO: set the image uri
                                                                                    NotificationModel notificationModel = new NotificationModel();
                                                                                    notificationModel.setFrom(currentUserId);
                                                                                    notificationModel.setTo(userId);
                                                                                    notificationModel.setType(NOTIF_LIKE);
                                                                                    notificationModel.setPostId(postId);
                                                                                    notificationModel.setDate(new Timestamp(new Date()));
                                                                                    if (!currentUserId.contentEquals(userId)) {
                                                                                        collectionReferenceNotif.document(currentUserId + "_" + NOTIF_LIKE + "_" + postId).set(notificationModel)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        notificationModel.setFromName(CurrentUserInfo.getInstance().getUserName());
                                                                                                        notificationModel.setFromPdp(CurrentUserInfo.getInstance().getPdpUrl());
                                                                                                        notificationModel.setToUsername(userName);
                                                                                                        notificationModel.setToPdp(pdpUrl);
                                                                                                        notificationModel.setImageNotified(posts.get(getAdapterPosition()).getImageUrl());
                                                                                                        sendNotif = new SendNotif(notificationModel);
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
                        .addToBackStack(null)
                        .commit();
            } else if (v.getId() == R.id.likes_nb) {
                createLikesPopup();


            }

        }

        private void createLikesPopup() {
            likes = new ArrayList<>();
            Log.d("POPUP", "createLikesPopup: ");
            builder = new AlertDialog.Builder(context);
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.likes_popup, null);
            recyclerView = view.findViewById(R.id.likes_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            collectionReference.whereEqualTo("postId", posts.get(getAdapterPosition()).getPostId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot likeDocument : queryDocumentSnapshots) {
                                collectionReferenceUsers.whereEqualTo("uid", likeDocument.getString("userId"))
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                                    String userId = userDocument.getString("uid");
                                                    String userName = userDocument.getString("username");
                                                    String pdpUrl = userDocument.getString("pdp");

                                                    LikesModel likesModel = new LikesModel(userId, userName, pdpUrl);
                                                    likes.add(likesModel);
                                                    Log.d("SIZE", "onSuccess: " + likes.size());

                                                }
                                                Log.d("SIZE", "onSuccess: " + likes.size());
                                                likesAdapter = new LikesAdapter(context, likes, fragmentManager, dialog);
                                                recyclerView.setAdapter(likesAdapter);
                                                likesAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }

                        }
                    });

            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            resizePopup(dialog);

        }


        private void resizePopup(AlertDialog dialog) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int displayHeight = displayMetrics.heightPixels;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            int dialogWindowHeight = (int) (displayHeight * 0.7f);
            layoutParams.height = dialogWindowHeight;
            dialog.getWindow().setAttributes(layoutParams);
        }


    }


}
