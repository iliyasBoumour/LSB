package com.inpt.lsb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.NotificationAdapter;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private RecyclerView notifRecView;
    private NotificationAdapter notificationAdapter;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressBar pb;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notifRecView = view.findViewById(R.id.notifRecyclerView);
        pb = view.findViewById(R.id.pb);
        notifRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getList();


        return view;
    }

    private void getList() {
        pb.setVisibility(View.VISIBLE);
        List<NotificationModel> notificationModels = new ArrayList<>();
        db.collection("Notifications")
                .whereEqualTo("to", currentUserInfo.getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        db.collection("users")
                                .document(doc.getString("from"))
                                .get()
                                .addOnSuccessListener(ds -> {
                                    if (doc.getString("postId") != null) {
                                        db.collection("Posts")
                                                .whereEqualTo("postId", doc.getString("postId"))
                                                .get()
                                                .addOnSuccessListener(documentSnapshots -> {
                                                    for (QueryDocumentSnapshot ds1 : documentSnapshots) {
                                                        pb.setVisibility(View.GONE);
                                                        NotificationModel model = new NotificationModel(doc.getString("from"), ds.getString("username"), ds.getString("pdp"), doc.getString("type"), doc.getString("postId"), ds1.getString("imageUrl"), doc.getTimestamp("date"));
                                                        notificationModels.add(model);
                                                        notificationAdapter = new NotificationAdapter(getActivity(), notificationModels);
                                                        notifRecView.setAdapter(notificationAdapter);
                                                        notificationAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                    }else{
                                        pb.setVisibility(View.GONE);
                                        NotificationModel model = new NotificationModel(doc.getString("from"), ds.getString("username"), ds.getString("pdp"), doc.getTimestamp("date"), doc.getString("type"));
                                        notificationModels.add(model);
                                        notificationAdapter = new NotificationAdapter(getActivity(), notificationModels);
                                        notifRecView.setAdapter(notificationAdapter);
                                        notificationAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
                    Log.i("eeeeee", "erreur");
                });

    }
}