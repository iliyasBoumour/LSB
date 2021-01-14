package com.inpt.adapters;

import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.inpt.lsb.R;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private List<NotificationModel> notifications=new ArrayList<>();
    private Context context;

    public NotificationAdapter(Context context) {
        this.context=context;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item,parent,false);
        ViewHolder holder=new ViewHolder(view,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.notificationText.setText(notifications.get(position).getNotification());
        holder.notificationDate.setText(notifications.get(position).getDate().toString());
        holder.pdp.setImageResource(R.drawable.post_image);
//        holder.notifiedImage.setImageResource(R.drawable.pdp);
        holder.item.setOnClickListener(view -> {
            Toast.makeText(context, notifications.get(position).getNotification(), Toast.LENGTH_SHORT).show();
        });
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

    //    this inner class generate our view objects (hold the view for each item in our recyclerView)
    public  class ViewHolder extends RecyclerView.ViewHolder{
        //in our contact_item_layout we had a TextView and a relativeLayout so we have to definet them here
        private ConstraintLayout item;
        private ImageView pdp;
        private TextView notificationText;
        private TextView notificationDate;
        private ImageView notifiedImage;
        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            pdp=itemView.findViewById(R.id.profile);
            notificationText=itemView.findViewById(R.id.notificationText);
            notificationDate=itemView.findViewById(R.id.notificationDate);
            notifiedImage=itemView.findViewById(R.id.image);
        }
    }

}
