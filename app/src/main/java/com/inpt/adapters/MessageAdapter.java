package com.inpt.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.R;
import com.inpt.models.MessageModel;
import com.inpt.models.Post;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private Context context;
    private List<MessageModel> messages;
    String pdpUrl;



    public MessageAdapter(Context context,List<MessageModel> messages, String pdpUrl) {
       /* Collections.sort(messages, new Comparator<MessageModel>() {
            @Override
            public int compare(MessageModel o1, MessageModel o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });*/
        this.context = context;
        this.messages = messages;
        this.pdpUrl = pdpUrl;
    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view, context);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
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


        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            msgTextView = itemView.findViewById(R.id.msg_text_view);
            pdp = itemView.findViewById(R.id.pdp);
        }


        @Override
        public void onClick(View v) {

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
