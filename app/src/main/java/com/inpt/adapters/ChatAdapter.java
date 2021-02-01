package com.inpt.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.R;
import com.inpt.models.MessageModel;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private Context context;
    private List<MessageModel> messages;
    String pdpUrl;
    String userName;
    String userId;



    public ChatAdapter(Context context, List<MessageModel> messages, String pdpUrl, String userName, String userId){
       /* Collections.sort(messages, new Comparator<MessageModel>() {
            @Override
            public int compare(MessageModel o1, MessageModel o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });*/
        this.userId = userId;
        this.userName = userName;
        this.context = context;
        this.messages = messages;
        this.pdpUrl = pdpUrl;
    }


    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view, context);

        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view, context);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        holder.msgTextView.setText(message.getMessage());
        Uri uri = Uri.parse(pdpUrl);
        holder.pdp.setImageURI(uri);

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }






    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView msgTextView;
        private SimpleDraweeView pdp;
        private Context ctx;


        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            this.ctx = context;
            msgTextView = itemView.findViewById(R.id.msg_text_view);
            pdp = itemView.findViewById(R.id.pdp);
            pdp.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.pdp) {
                Intent intent = new Intent(ctx, DashboardActivity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("pdpUrl", pdpUrl);
                intent.putExtra("userId",userId );
                intent.putExtra("Fragment", "profileOtherUsers");
                v.getContext().startActivity(intent);
            }

        }

    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderId().contentEquals(CurrentUserInfo.getInstance().getUserId())) {
            return RIGHT;
        } else {
            return LEFT;
        }
    }
}
