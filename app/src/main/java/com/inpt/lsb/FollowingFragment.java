package com.inpt.lsb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.LikesAdapter;
import com.inpt.models.LikesModel;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<LikesModel> followingList;
    private LikesAdapter likesAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private CollectionReference collectionReferenceRelations = db.collection("Relations");
    private String currentUserId;
    private String userId;



    public FollowingFragment() {

    }

    public FollowingFragment(String userId) {
         this.userId = userId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.following_fragment, container, false);
        recyclerView = view.findViewById(R.id.followingRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getfollowingList();
        return view;
    }

    private void getfollowingList() {
        followingList = new ArrayList<>();
        collectionReferenceRelations.whereEqualTo("userFollowerId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot relationDocument : queryDocumentSnapshots) {
                            collectionReferenceUsers.whereEqualTo("uid", relationDocument.getString("userFollowedId"))
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for(QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                                String userId = userDocument.getString("uid");
                                                String userName = userDocument.getString("username");
                                                String pdpUrl = userDocument.getString("pdp");

                                                LikesModel likesModel = new LikesModel(userId, userName, pdpUrl);
                                                followingList.add(likesModel);

                                            }
                                            if(getActivity() != null) {
                                                likesAdapter = new LikesAdapter(getActivity(), followingList, getActivity().getSupportFragmentManager(), null);
                                                recyclerView.setAdapter(likesAdapter);
                                                likesAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    }
                });




    }
}
