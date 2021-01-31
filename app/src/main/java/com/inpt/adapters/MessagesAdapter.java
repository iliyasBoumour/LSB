package com.inpt.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.ChatActivity;
import com.inpt.lsb.R;
import com.inpt.models.LastMessageModel;
import com.inpt.models.MessageModel;
import com.inpt.models.Post;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>{

    private Context context;
    private List<LastMessageModel> lastMessageModels;




    public MessagesAdapter(Context context, List<LastMessageModel> lastMessageModels) {
        this.context = context;
        this.lastMessageModels = lastMessageModels;
    }


    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_item, parent, false);
            return new ViewHolder(view, context);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LastMessageModel lastMessageModel = lastMessageModels.get(position);
        holder.time.setText((String) DateUtils.getRelativeTimeSpanString(lastMessageModel.getTime() * 1000));
        holder.messageTextView.setText(lastMessageModel.getMessage());
        holder.userNameTextView.setText(lastMessageModel.getUserName());
        Uri uri = Uri.parse(lastMessageModel.getPdpUrl());
        holder.pdp.setImageURI(uri);


    }

    @Override
    public int getItemCount() {
        return lastMessageModels.size();
    }






    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

       public SimpleDraweeView pdp;
       public TextView userNameTextView, messageTextView, time;
       public RelativeLayout item;
       Context ctx;


        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            this.ctx = context;
            pdp = itemView.findViewById(R.id.pdp);
            userNameTextView = itemView.findViewById(R.id.userName);
            messageTextView = itemView.findViewById(R.id.last_msg);
            time = itemView.findViewById(R.id.time);
            item = itemView.findViewById(R.id.item);
            pdp.setOnClickListener(this);
            item.setOnClickListener(this);
            messageTextView.setOnClickListener(this);
            userNameTextView.setOnClickListener(this);


        }


        @Override
        public void onClick(View v) {
             if(v.getId() == R.id.pdp ||v.getId() == R.id.userName || v.getId() == R.id.item || v.getId() == R.id.last_msg  ) {
                 Intent intent = new Intent(ctx, ChatActivity.class);
                 intent.putExtra("userId", lastMessageModels.get(getAdapterPosition()).getUserId());
                 intent.putExtra("userName", lastMessageModels.get(getAdapterPosition()).getUserName());
                 intent.putExtra("pdpUrl", lastMessageModels.get(getAdapterPosition()).getPdpUrl());
                 v.getContext().startActivity(intent);
             }
        }

    }


}