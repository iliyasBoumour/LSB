package com.inpt.lsb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inpt.adapters.ProfileAdapter;
import com.inpt.models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ProfileOtherUsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Post> posts;

    public ProfileOtherUsersFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_other_users_fragment, container, false);
        recyclerView = view.findViewById(R.id.ProfilerecyclerView);
        posts = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2, GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        profileAdapter = new ProfileAdapter(getActivity(), posts, (Objects.requireNonNull(getActivity())).getSupportFragmentManager());
        recyclerView.setAdapter(profileAdapter);
        return view;
    }
}