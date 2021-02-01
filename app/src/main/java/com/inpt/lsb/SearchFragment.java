package com.inpt.lsb;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inpt.adapters.SearchAdapter;
import com.inpt.models.SearchResModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SearchFragment extends Fragment {

    private RecyclerView serchRes;
    private SearchView searchView;
    private ImageView backBtn;
    private List<SearchResModel> searchResModels;
    private SearchAdapter searchAdapter;
    private ProgressBar progressBar;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        serchRes=view.findViewById(R.id.serchRes);
        searchView=view.findViewById(R.id.searchKey);
        progressBar = view.findViewById(R.id.progressBar);
        Handler handler = new Handler();

        searchResModels=new ArrayList<>();

        serchRes.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchAdapter=new SearchAdapter(getActivity());

        searchAdapter.setSearchResModels(searchResModels);
        serchRes.setAdapter(searchAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())) searchUsers(s);
                else clearList();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                if (!TextUtils.isEmpty(s.trim())) {
                            searchUsers(s.toLowerCase());
                }
                else clearList();
                }, 600);
                return false;
            }
        });
        return view;
    }

    private void searchUsers(String s) {
        progressBar.setVisibility(View.VISIBLE);
        clearList();
        FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("searchName")
                .startAt(s)
                .endAt(s + "\uf8ff")
                .get()
                .addOnCompleteListener(t->{
                    progressBar.setVisibility(View.GONE);
                    for (DocumentSnapshot doc : t.getResult()){
                        SearchResModel model=new SearchResModel(doc.getString("uid"),doc.getString("username"),doc.getString("pdp"));
                        searchResModels.add(model);
                    }
                    if (searchAdapter!=null && getActivity() != null) {
                        searchAdapter = new SearchAdapter(getActivity(), searchResModels, (Objects.requireNonNull(getActivity())).getSupportFragmentManager());
                        serchRes.setAdapter(searchAdapter);
                    }
                })
                .addOnFailureListener(e->{
                    progressBar.setVisibility(View.GONE);
                    Log.i("eeeeee","erreur");
                });

    }
    private void clearList(){
        searchResModels.clear();
        searchAdapter.notifyDataSetChanged();
    }
}