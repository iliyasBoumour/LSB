package com.inpt.lsb;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.inpt.adapters.SearchAdapter;
import com.inpt.models.SearchResModel;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private RecyclerView serchRes;
    private EditText searchKeyEt;
    private ImageView searchBtn,backBtn;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        serchRes=view.findViewById(R.id.serchRes);
        searchKeyEt=view.findViewById(R.id.searchKey);
        searchBtn=view.findViewById(R.id.searchBtn);
        backBtn=view.findViewById(R.id.backBtn);

        List<SearchResModel> searchResModels=new ArrayList<>();
        searchResModels.add(new SearchResModel("weeeeeee1"));
        searchResModels.add(new SearchResModel("weeeeeee2"));

        serchRes.setLayoutManager(new LinearLayoutManager(getActivity()));
        SearchAdapter searchAdapter=new SearchAdapter(getActivity());
        searchAdapter.setSearchResModels(searchResModels);
        serchRes.setAdapter(searchAdapter);
        return view;
    }
}