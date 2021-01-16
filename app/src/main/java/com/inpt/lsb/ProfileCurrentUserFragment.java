package com.inpt.lsb;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.ProfileAdapter;

import java.util.ArrayList;
import java.util.List;


public class ProfileCurrentUserFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Integer> posts;
    private String currentUserId;
    private String currentUserName;

    public ProfileCurrentUserFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        currentUserName = CurrentUserInfo.getInstance().getUserName();
        Log.d("HOME", "onCreate: " + currentUserId);
        Log.d("HOME", "onCreate: " + currentUserName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_current_user_fragment, container, false);
        recyclerView = view.findViewById(R.id.ProfilerecyclerView);
        posts = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            posts.add(i);
        }
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2, GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        profileAdapter = new ProfileAdapter(getActivity(), posts);
        recyclerView.setAdapter(profileAdapter);
        return view;
    }
}