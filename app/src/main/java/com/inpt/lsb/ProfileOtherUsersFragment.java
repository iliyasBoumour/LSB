package com.inpt.lsb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.SendNotif;
import com.inpt.models.NotificationModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ProfileOtherUsersFragment extends Fragment implements View.OnClickListener {

    private String userId;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceFollow = db.collection("Relations");
    private CollectionReference collectionReferenceNotif = db.collection("Notifications");
    private SimpleDraweeView pdp;
    private TextView userNameTextView;
    private ImageView messagesBtn;
    private String pdpUrl;
    private String userName;
    private MaterialButton followBtn, messageBtn;
    private Boolean follow;
    private static final String NOTIF_FOLLOW = "follow";
    SendNotif sendNotif;

    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        messageBtn = view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(this);
        followBtn = view.findViewById(R.id.follow_btn);
        messagesBtn = view.findViewById(R.id.messages_btn);
        messagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MessagesActivity.class));
            }
        });
        followBtn.setOnClickListener(this);
        userNameTextView = view.findViewById(R.id.userName);
        Bundle bundle = this.getArguments();
        pdpUrl = bundle.getString("pdpUrl");
        Uri uri = Uri.parse(pdpUrl);
        userName = bundle.getString("userName");
        userNameTextView.setText(userName);
        pdp.setImageURI(uri);

        checkFollow();

        //TABS
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);
        if(getActivity() != null) {
            Log.d("TAB", "onCreateView: ");

            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0, userId);
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_posts);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_follower);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_following);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        tabLayout.setupWithViewPager(viewPager);
        if(getActivity() != null) {
            Log.d("TAB", "onCreateView: ");

            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0, userId);
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_posts);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_follower);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_following);
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
       private String userId;



        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, String userId) {
            super(fm, behavior);
            this.userId = userId;
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PostsFragment(userId, userName, pdpUrl);
                case 1:
                    return new FollowersFragment(userId) ;
                case 2:
                    return new FollowingFragment(userId);
            }
            return null;

        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Posts";
                case 1:
                    return "Followers";
                case 2:
                    return "Following";
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
        }
    }


    private void checkFollow() {
        collectionReferenceFollow.document(currentUserId + "_" + userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
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
        if(follow) {
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
            case R.id.message_btn:
                if(getActivity() != null) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("pdpUrl", pdpUrl);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userId", userId);
                    getActivity().startActivity(intent);
                }
                break;
        }
    }

}