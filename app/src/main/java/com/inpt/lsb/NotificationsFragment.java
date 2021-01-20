package com.inpt.lsb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.inpt.adapters.NotificationAdapter;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private RecyclerView notifRecView;
    private NotificationAdapter notificationAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notifRecView = view.findViewById(R.id.notifRecyclerView);
        List<NotificationModel> notifs=new ArrayList<>();
        notifs.add(new NotificationModel("weeeeeee1","weeeeeee1","weeeeeee1","weeeeeee1",new Timestamp(new Date())));
        notifs.add(new NotificationModel("weeeeeee2","weeeeeee1","weeeeeee1","weeeeeee1",new Timestamp(new Date())));

        notifRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationAdapter=new NotificationAdapter(getActivity());
        notificationAdapter.setNotifications(notifs);
        notifRecView.setAdapter(notificationAdapter);
        return view;
    }
}