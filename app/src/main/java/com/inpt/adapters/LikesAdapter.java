package com.inpt.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.SendNotif;
import com.inpt.lsb.ProfileCurrentUserFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.LikesModel;
import com.inpt.models.NotificationModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.ViewHolder> {

    private Context context;
    private List<LikesModel> likes;
    private FragmentManager fragmentManager;
    private androidx.appcompat.app.AlertDialog dialog;
    private String tagFragment;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceFollow = db.collection("Relations");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");
    private static final String NOTIF_FOLLOW = "follow";
    SendNotif sendNotif;







    public LikesAdapter(Context context, List<LikesModel> likes, FragmentManager fragmentManager, AlertDialog dialog) {
        this.context = context;
        this.likes = likes;
        this.fragmentManager = fragmentManager;
        this.dialog = dialog;

    }

    @NonNull
    @Override
    public LikesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.like_item, parent, false);

        return new ViewHolder(view, context, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull LikesAdapter.ViewHolder holder, int position) {
        String pdpUrl = likes.get(position).getPdpUrl();
        String userName = likes.get(position).getUserName();
        Uri uri = Uri.parse(pdpUrl);
        holder.userNameTextView.setText(userName);
        holder.pdp.setImageURI(uri);
        holder.checkFollow();
        if(likes.get(position).getUserId().contentEquals(CurrentUserInfo.getInstance().getUserId())) {
            holder.followBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return likes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public SimpleDraweeView pdp;
        public TextView userNameTextView;
        public MaterialButton followBtn;
        FragmentManager fragmentManager;
        Fragment fragment;
        Boolean follow;


        public ViewHolder(@NonNull final View itemView, final Context ctx, final FragmentManager f) {
            super(itemView);
            context = ctx;
            fragmentManager = f;
            pdp = itemView.findViewById(R.id.pdpImageView);
            userNameTextView = itemView.findViewById(R.id.userName_textView);
            followBtn = itemView.findViewById(R.id.followBtn);
            pdp.setOnClickListener(this);
            userNameTextView.setOnClickListener(this);
            followBtn.setOnClickListener(this);

            }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.pdpImageView || v.getId() == R.id.userName_textView) {
                fragment = new ProfileOtherUsersFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userName", likes.get(getAdapterPosition()).getUserName());
                bundle.putString("pdpUrl", likes.get(getAdapterPosition()).getPdpUrl());
                bundle.putString("userId", likes.get(getAdapterPosition()).getUserId());
                if(likes.get(getAdapterPosition()).getUserId().contentEquals(CurrentUserInfo.getInstance().getUserId())) {
                    fragment = new ProfileCurrentUserFragment();
                } else  {
                    fragment = new ProfileOtherUsersFragment();
                    fragment.setArguments(bundle);
                }
                if(dialog != null)  dialog.dismiss();
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .addToBackStack(null)
                        .commit();

            } else if(v.getId() == R.id.followBtn) {
                followUser();
            }

        }

        public void checkFollow() {
            String currentUserId = CurrentUserInfo.getInstance().getUserId();
            String userId = likes.get(getAdapterPosition()).getUserId();


            collectionReferenceFollow.document(currentUserId + "_" + userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()) {
                                followBtn.setText("Unfollow");
                                followBtn.setTextColor(context.getResources().getColor(R.color.orange));
                                follow = true;
                            } else {
                                followBtn.setWidth(followBtn.getWidth());
                                followBtn.setText("Follow");
                                followBtn.setTextColor(context.getResources().getColor(R.color.white));
                                followBtn.setBackgroundColor(context.getResources().getColor(R.color.orange));
                                follow = false;
                            }
                        }
                    });
        }

        private  void followUser() {
            String currentUserId = CurrentUserInfo.getInstance().getUserId();
            String userId = likes.get(getAdapterPosition()).getUserId();
            if(follow) {
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
                                NotificationModel notificationModel = new NotificationModel();
                                notificationModel.setFrom(currentUserId);
                                notificationModel.setTo(userId);
                                notificationModel.setType(NOTIF_FOLLOW);
                                notificationModel.setPostId(null);
                                notificationModel.setDate(new Timestamp(new Date()));
                                collectionReferenceNotif.document(currentUserId + "_" + NOTIF_FOLLOW + "_" + userId).set(notificationModel)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                notificationModel.setFromName(CurrentUserInfo.getInstance().getUserName());
                                                notificationModel.setFromPdp(CurrentUserInfo.getInstance().getPdpUrl());
                                                sendNotif=new SendNotif(notificationModel);
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
            if(follow) {
                followBtn.setText("Unfollow");
                followBtn.setTextColor(context.getResources().getColor(R.color.orange));
                followBtn.setBackgroundColor(Color.TRANSPARENT);
            } else {
                followBtn.setText("Follow");
                followBtn.setTextColor(context.getResources().getColor(R.color.white));
                followBtn.setBackgroundColor(context.getResources().getColor(R.color.orange));
                followBtn.setWidth(followBtn.getWidth());
            }
        }


    }
    }

