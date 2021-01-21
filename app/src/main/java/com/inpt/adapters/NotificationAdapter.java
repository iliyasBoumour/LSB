package com.inpt.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
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

    public NotificationAdapter(Context context,List<NotificationModel> notifications) {
        Collections.sort(notifications,
                (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        this.context=context;
        this.notifications=notifications;
    }


    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item,parent,false);
        ViewHolder holder=new ViewHolder(view,context);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        TODO : fresco
        Glide.with(context)
                .load(notifications.get(position).getFromPdp())
                .transform(new CircleCrop())
                .into(holder.pdp);
        switch (notifications.get(position).getType()){
            case NOTIF_LIKE:
                holder.notificationText.setText(notifications.get(position).getFromName()+" likes your post");
                Glide.with(context)
                        .load(notifications.get(position).getImageNotified())
                        .into(holder.notifiedImage);
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


    public  class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout item;
        private ImageView pdp;
        private TextView notificationText;
        private TextView notificationDate;
        private ImageView notifiedImage;
        private CircularRevealCardView notifiedImageContainer;
        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            pdp=itemView.findViewById(R.id.profile);
            notificationText=itemView.findViewById(R.id.notificationText);
            notificationDate=itemView.findViewById(R.id.notificationDate);
            notifiedImage=itemView.findViewById(R.id.image);
            notifiedImageContainer=itemView.findViewById(R.id.notifiedImage);
        }
    }

}
