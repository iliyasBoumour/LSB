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

public class PostsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Post> posts;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Posts");
    private String userId;
    private String pdpUlrl, userName;


    public PostsFragment() {

    }

    public PostsFragment(String userId, String userName, String pdpUrl) {

        this.userId = userId;
        this.userName = userName;
        this.pdpUlrl = pdpUrl;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.posts_fragment, container, false);
        recyclerView = view.findViewById(R.id.postsRecycler);
        posts = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2, GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        getPosts();
        return view;
    }


    private void getPosts() {
        collectionReference.whereEqualTo("userId", userId)
                .orderBy("timeAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for(QueryDocumentSnapshot post_ : queryDocumentSnapshots) {
                                Log.d("TEST", "onSuccess: ");

                                Post post = post_.toObject(Post.class);
                                Log.d("TEST", "onSuccess: " + post.getImageUrl());
                                posts.add(post);
                            }
                            Log.d("TEST111", "onSuccess: " + getActivity());
                            if(getActivity() != null) {
                                profileAdapter = new ProfileAdapter(getActivity(), posts, (getActivity()).getSupportFragmentManager(), userName, pdpUlrl);
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


}
