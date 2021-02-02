package com.inpt.lsb;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.DoubleClickListener;
import com.inpt.Util.SendNotif;
import com.inpt.adapters.LikesAdapter;
import com.inpt.models.LikesModel;
import com.inpt.models.NotificationModel;
import com.inpt.models.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class PostFragment extends Fragment implements View.OnClickListener{
    private SimpleDraweeView simpleDraweeView, pdpImage;
    private TextView captionTextView, timeTextView, likesTextView, userNameTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private ImageView like_icone, threeDotMenu, back;
    private String currentUserId;
    private String postId;
    private String postImageUrl;
    private Boolean isliked;
    private String userId;
    private int nbLike;
    private String caption;
    private EditText captionField;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private CollectionReference collectionReference = db.collection("Likes");
    private CollectionReference collectionReferencePost = db.collection("Posts");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");
    private MaterialButton cancelBtn, confirmBtn;

    private static final String NOTIF_LIKE="like";
    SendNotif sendNotif;
    private Post post;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    RecyclerView recyclerView;
    List<LikesModel> likes;
    LikesAdapter likesAdapter;

    String userName;
    String pdpUrl;



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
        likesTextView.setOnClickListener(this);
        like_icone = view.findViewById(R.id.like);
        timeTextView = view.findViewById(R.id.time);
        back = view.findViewById(R.id.back);
        back.setOnClickListener(this);
        threeDotMenu = view.findViewById(R.id.threeDotMenu);
        if(!currentUserId.contentEquals(userId)) {
            threeDotMenu.setVisibility(View.INVISIBLE);
        }
        threeDotMenu.setOnClickListener(this);
        pdpImage = view.findViewById(R.id.pdp_imageView);
        pdpImage.setOnClickListener(this);
        userNameTextView = view.findViewById(R.id.userName_textView);
        userNameTextView.setOnClickListener(this);
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
        userName = bundle.getString("userName");
        pdpUrl = bundle.getString("pdpUrl");
        userNameTextView.setText(userName);
        Uri uriPdp = Uri.parse(pdpUrl);
        pdpImage.setImageURI(uriPdp);
        getPost(postId);




        return view;
    }



    @Override
    public void onClick(View v) {

        if((v.getId() == R.id.pdp_imageView || v.getId() == R.id.userName_textView) && getActivity() != null ) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Fragment fragment;
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putString("userName", userName);
            bundle.putString("pdpUrl", pdpUrl);
            if(userId.contentEquals(currentUserId)) {
                fragment = new ProfileCurrentUserFragment();
            } else  {
                fragment = new ProfileOtherUsersFragment();
                fragment.setArguments(bundle);
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.homeFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
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
                                    createConfirmationPopup();

                                    break;
                                case R.id.edit:
                                    createEditPostPopup();
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                    break;
                case R.id.likes_nb:
                    createLikesPopup();
                    break;
                case R.id.back:
                    if(getActivity() != null) {
                        if(this.getArguments().getString("From") != null) {
                            getActivity().finish();
                        } else  {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                    break;
            }
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
//                                                                                TODO: set the image uri
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
                                                                                                    notificationModel.setFromName(CurrentUserInfo.getInstance().getUserName());
                                                                                                    notificationModel.setFromPdp(CurrentUserInfo.getInstance().getPdpUrl());
                                                                                                    notificationModel.setImageNotified(postImageUrl);
                                                                                                    notificationModel.setToUsername(userName);
                                                                                                    notificationModel.setToPdp(pdpUrl);
                                                                                                    sendNotif=new SendNotif(notificationModel);
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
                        try{
                            caption = post.getCaption();
                            postImageUrl = post.getImageUrl();
                            int nbLike = post.getNbLike();
                            String time = (String) DateUtils.getRelativeTimeSpanString(post.getTimeAdded().getSeconds() * 1000);
                            Uri uri = Uri.parse(postImageUrl);
                            captionTextView.setText(caption);
                            simpleDraweeView.setImageURI(uri);
                            likesTextView.setText(setnbLike(nbLike));
                            timeTextView.setText(time);
                        }catch (NullPointerException e){
                            simpleDraweeView.setImageResource(R.drawable.error);
                            like_icone.setVisibility(View.GONE);
                            threeDotMenu.setVisibility(View.GONE);
                        }
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

    private void createLikesPopup() {
        likes= new ArrayList<>();
        Log.d("POPUP", "createLikesPopup: ");
        builder = new AlertDialog.Builder(getActivity());
        View view = ((Activity)getActivity()).getLayoutInflater().inflate(R.layout.likes_popup, null);
        recyclerView = view.findViewById(R.id.likes_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        collectionReference.whereEqualTo("postId", postId)
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
                                            for(QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                                String userId = userDocument.getString("uid");
                                                String userName = userDocument.getString("username");
                                                String pdpUrl = userDocument.getString("pdp");

                                                LikesModel likesModel = new LikesModel(userId, userName, pdpUrl);
                                                likes.add(likesModel);
                                                Log.d("SIZE", "onSuccess: " + likes.size());

                                            }
                                            Log.d("SIZE", "onSuccess: " + likes.size());
                                            if(getActivity() != null) {
                                                likesAdapter = new LikesAdapter(getActivity(), likes, getActivity().getSupportFragmentManager(), dialog);
                                                recyclerView.setAdapter(likesAdapter);
                                                likesAdapter.notifyDataSetChanged();
                                            }
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
        ((Activity)getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowHeight = (int) (displayHeight * 0.7f);
        layoutParams.height = dialogWindowHeight;
        dialog.getWindow().setAttributes(layoutParams);
    }

    private void createEditPostPopup() {
        builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.edit_post_popup, null);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        confirmBtn = view.findViewById(R.id.confirmBtn);
        captionField = view.findViewById(R.id.caption_field);
        captionField.setText(caption);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editPost(postId, dialog, captionField.getText().toString().trim());
            }
        });
        builder.setView(view);
        dialog = builder.create(); // creating dialog object
        dialog.show();
    }

    private void createConfirmationPopup() {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getActivity(), R.style.MyAlertDialogStyle);
        progressDialog.setMessage("Deleting ...");
        builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.confirmation_popup, null);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        confirmBtn = view.findViewById(R.id.confirmBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost(postId);
                dialog.dismiss();
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                       if(getActivity() != null) {
                           if(getArguments().getString("From") != null) {
                               getActivity().finish();
                           } else {
                               getActivity().getSupportFragmentManager().popBackStack();

                           }
                       }
                    }
                }, 2000);


            }
        });
        builder.setView(view);
        dialog = builder.create(); // creating dialog object
        dialog.show();

    }

    private void editPost(String postId, AlertDialog dialog, String newCaption) {
        collectionReferencePost.whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("caption", newCaption);
                            postDocument.getReference().update(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            getPost(postId);
                                            dialog.dismiss();
                                        }
                                    });

                        }
                    }
                });
    }






}
