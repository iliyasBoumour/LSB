package com.inpt.lsb;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.inpt.adapters.HomeAdapter;
import com.inpt.adapters.ProfileAdapter;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private List<Integer> posts;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        /*Button logout=view.findViewById(R.id.logout);
        logout.setOnClickListener(E->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(),LandingActivity.class));
            ((Activity)getActivity()).finish();
        });*/
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