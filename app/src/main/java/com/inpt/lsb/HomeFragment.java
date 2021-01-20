package com.inpt.lsb;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.HomeAdapter;
import com.inpt.models.Post;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private List<Post> posts;
    private String currentUserId;
    private String currentUserName;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceLikes = db.collection("Likes");
    private CollectionReference collectionReferencePosts = db.collection("Posts");
    private CollectionReference collectionReferenceRelations = db.collection("Relations");



    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
/*
        currentUserId = "JVSycE5aYHZhX4Q1Em8cfRQKkKW2";
*/
        currentUserName = CurrentUserInfo.getInstance().getUserName();
        Log.d("HOME", "onCreate: " + currentUserId);
        Log.d("HOME", "onCreate: " + currentUserName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        recyclerView = view.findViewById(R.id.homeRecyclerView);
        posts = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getPosts();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void getPosts() {
        Log.d("TEST", "getPosts: " + currentUserId);
        collectionReferenceRelations.whereEqualTo("userFollowerId", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for(QueryDocumentSnapshot followedDocument : queryDocumentSnapshots) {
                                collectionReferencePosts.whereEqualTo("userId", followedDocument.getString("userFollowedId"))
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                if(!queryDocumentSnapshots.isEmpty()) {
                                                    for (QueryDocumentSnapshot postDocument : queryDocumentSnapshots) {
                                                        Post post = postDocument.toObject(Post.class);
                                                        posts.add(post);
                                                        Log.d("TEST", "onSuccess: " + postDocument.get("caption"));
                                                    }
                                                }
                                                homeAdapter = new HomeAdapter(getActivity(), posts, (getActivity()).getSupportFragmentManager());
                                                recyclerView.setAdapter(homeAdapter);
                                                homeAdapter.notifyDataSetChanged();
                                                Log.d("TEST", "onSuccess: size" + posts.size());
                                            }
                                        });
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

}
