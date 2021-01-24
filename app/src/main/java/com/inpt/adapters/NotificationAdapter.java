package com.inpt.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.PostFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private List<NotificationModel> notifications=new ArrayList<>();
    private Context context;
    private static final String NOTIF_LIKE="like";
    private static final String NOTIF_FOLLOW="follow";
    Handler handler = new Handler();
    private FragmentManager fragmentManager;


    public NotificationAdapter(Context context,List<NotificationModel> notifications, FragmentManager fragmentManager) {
        Collections.sort(notifications,
                (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        this.context=context;
        this.notifications=notifications;
        this.fragmentManager = fragmentManager;
    }


    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item,parent,false);
        ViewHolder holder=new ViewHolder(view,context, fragmentManager);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pdp.setImageURI(notifications.get(position).getFromPdp());
        switch (notifications.get(position).getType()){
            case NOTIF_LIKE:
                holder.notificationText.setText(notifications.get(position).getFromName()+" likes your post");
                holder.notifiedImage.setImageURI(notifications.get(position).getImageNotified());
                break;
            case NOTIF_FOLLOW:
                holder.notificationText.setText(notifications.get(position).getFromName()+" starts following you");
                holder.notifiedImage.setVisibility(View.GONE);
                holder.notifiedImageContainer.setVisibility(View.GONE);
                break;
        }
        String time = (String) DateUtils.getRelativeTimeSpanString(notifications.get(position).getDate().getSeconds() * 1000);
        holder.notificationDate.setText(time);
//        holder.item.setOnClickListener(view -> {
//            Toast.makeText(context, notifications.get(position).getFromName(), Toast.LENGTH_SHORT).show();
//        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public List<NotificationModel> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationModel> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }


    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        FragmentManager fragmentManager;
        Fragment fragment;
        private ConstraintLayout item;
        private SimpleDraweeView pdp, notifiedImage;
        private TextView notificationText;
        private TextView notificationDate;
        private CircularRevealCardView notifiedImageContainer;
        public ViewHolder(@NonNull View itemView,Context context, final FragmentManager f) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            pdp=itemView.findViewById(R.id.profile);
            notificationText=itemView.findViewById(R.id.notificationText);
            notificationDate=itemView.findViewById(R.id.notificationDate);
            notifiedImage=itemView.findViewById(R.id.image);
            notifiedImageContainer=itemView.findViewById(R.id.notifiedImage);
            item.setOnClickListener(this);
            pdp.setOnClickListener(this);
            notificationText.setOnClickListener(this);
            notifiedImage.setOnClickListener(this);
            fragmentManager = f;

        }


        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.profile
                    || (v.getId() == R.id.item && notifications.get(getAdapterPosition()).getType().contentEquals(NOTIF_FOLLOW))
                    || (v.getId() == R.id.notificationText && notifications.get(getAdapterPosition()).getType().contentEquals(NOTIF_FOLLOW))) {
                fragment = new ProfileOtherUsersFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userName", notifications.get(getAdapterPosition()).getFromName());
                bundle.putString("pdpUrl", notifications.get(getAdapterPosition()).getFromPdp());
                bundle.putString("userId", notifications.get(getAdapterPosition()).getFrom());
                fragment.setArguments(bundle);

            }else if((v.getId() == R.id.notificationText && notifications.get(getAdapterPosition()).getType().contentEquals(NOTIF_LIKE))
                    || v.getId() == R.id.image
                    || (v.getId() == R.id.item && notifications.get(getAdapterPosition()).getType().contentEquals(NOTIF_LIKE))) {
                fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString("postId", notifications.get(getAdapterPosition()).getPostId());
                bundle.putString("userName", CurrentUserInfo.getInstance().getUserName());
                bundle.putString("pdpUrl", CurrentUserInfo.getInstance().getPdpUrl());
                bundle.putString("userId", notifications.get(getAdapterPosition()).getTo());
                fragment.setArguments(bundle);

            }

            fragmentManager.beginTransaction()
                    .replace(R.id.homeFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
