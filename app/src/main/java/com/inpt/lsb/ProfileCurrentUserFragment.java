package com.inpt.lsb;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.ProfileAdapter;
import com.inpt.models.Post;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


public class ProfileCurrentUserFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Post> posts;
    private String currentUserId;
    private Button edite_btn;
    private ImageView messagesBtn;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Posts");

    private SimpleDraweeView pdp;
    private TextView userNameTextView;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    public ProfileCurrentUserFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();



    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_current_user_fragment, container, false);
        pdp = view.findViewById(R.id.pdp_imageView);
        userNameTextView = view.findViewById(R.id.userName);
        messagesBtn = view.findViewById(R.id.messages_btn);
        messagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MessagesActivity.class));
            }
        });
        edite_btn=view.findViewById(R.id.edite_btn);
        edite_btn.setOnClickListener(e->startActivity(new Intent(getActivity(),EditProfileActivity.class)));
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        userNameTextView.setText(CurrentUserInfo.getInstance().getUserName());
        Uri uri = Uri.parse(CurrentUserInfo.getInstance().getPdpUrl());
        pdp.setImageURI(uri);


        //TABS
        tabLayout.setupWithViewPager(viewPager);
        if(getActivity() != null) {
            Log.d("TAB", "onCreateView: ");

            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0, currentUserId);
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
                    return new PostsFragment(userId, CurrentUserInfo.getInstance().getUserName(), CurrentUserInfo.getInstance().getPdpUrl());
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
}