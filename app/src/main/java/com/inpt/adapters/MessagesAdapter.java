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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.drawable.ScaleTypeDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
    public static final float ONLINE = 4.0f;
    public static final float OFFLINE = 0.0f;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");





    public MessagesAdapter(Context context, List<LastMessageModel> lastMessageModels) {
        Collections.sort(lastMessageModels, new Comparator<LastMessageModel>() {
            @Override
            public int compare(LastMessageModel o1, LastMessageModel o2) {
                return Long.compare(o2.getTime(), o1.getTime());
            }
        });
        this.context = context;
        this.lastMessageModels = lastMessageModels;
    }


    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_item, parent, false);
            return new ViewHolder(view, context);


    }

    private  String checkMessageSize(String message) {
        if(message.length() > 10) {
            return message.substring(0, 9).concat("...");
        } else {
            return message;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LastMessageModel lastMessageModel = lastMessageModels.get(position);
        holder.time.setText((String) DateUtils.getRelativeTimeSpanString(lastMessageModel.getTime() * 1000));
        holder.messageTextView.setText(checkMessageSize(lastMessageModel.getMessage()));
        holder.userNameTextView.setText(lastMessageModel.getUserName());
        Uri uri = Uri.parse(lastMessageModel.getPdpUrl());
        holder.pdp.setImageURI(uri);

        collectionReferenceUsers.whereEqualTo("uid", lastMessageModel.getUserId())
               .addSnapshotListener(new EventListener<QuerySnapshot>() {
                   @Override
                   public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                       if(error == null && value != null) {
                           for(QueryDocumentSnapshot userDocument : value) {
                              if(userDocument.getString("status") != null) {
                                  switch (userDocument.getString("status")) {
                                      case "online":
                                          setStatus(holder.pdp, ONLINE);
                                          break;
                                      case "offline":
                                          setStatus(holder.pdp, OFFLINE);
                                          break;
                                  }
                              }
                           }
                       }
                   }
               });

    }


    @Override
    public int getItemCount() {
        return lastMessageModels.size();
    }

    public void setStatus(SimpleDraweeView simpleDraweeView, float width) {

        int color = context.getResources().getColor(R.color.green);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setBorder(color, width);
        roundingParams.setRoundAsCircle(true);
        simpleDraweeView.getHierarchy().setRoundingParams(roundingParams);

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
