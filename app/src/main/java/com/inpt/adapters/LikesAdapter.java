package com.inpt.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.PostFragment;
import com.inpt.lsb.ProfileCurrentUserFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.LikesModel;
import com.inpt.models.Post;

import java.util.List;



public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.ViewHolder> {

    private Context context;
    private List<LikesModel> likes;
    private FragmentManager fragmentManager;
    private androidx.appcompat.app.AlertDialog dialog;




    public LikesAdapter(Context context, List<LikesModel> likes, FragmentManager fragmentManager, AlertDialog dialog) {
        this.context = context;
        this.likes = likes;
        this.fragmentManager = fragmentManager;
        this.dialog = dialog;

    }

    @NonNull
    @Override
    public LikesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.like_item, parent, false);

        return new ViewHolder(view, context, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull LikesAdapter.ViewHolder holder, int position) {
        String pdpUrl = likes.get(position).getPdpUrl();
        String userName = likes.get(position).getUserName();
        Uri uri = Uri.parse(pdpUrl);
        holder.userNameTextView.setText(userName);
        holder.pdp.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return likes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public SimpleDraweeView pdp;
        public TextView userNameTextView;
        FragmentManager fragmentManager;
        Fragment fragment;

        public ViewHolder(@NonNull final View itemView, final Context ctx, final FragmentManager f) {
            super(itemView);
            context = ctx;
            fragmentManager = f;
            pdp = itemView.findViewById(R.id.pdpImageView);
            userNameTextView = itemView.findViewById(R.id.userName_textView);
            pdp.setOnClickListener(this);
            userNameTextView.setOnClickListener(this);

            }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.pdpImageView || v.getId() == R.id.userName_textView) {
                fragment = new ProfileOtherUsersFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userName", likes.get(getAdapterPosition()).getUserName());
                bundle.putString("pdpUrl", likes.get(getAdapterPosition()).getPdpUrl());
                bundle.putString("userId", likes.get(getAdapterPosition()).getUserId());
                if(likes.get(getAdapterPosition()).getUserId().contentEquals(CurrentUserInfo.getInstance().getUserId())) {
                    fragment = new ProfileCurrentUserFragment();
                } else  {
                    fragment = new ProfileOtherUsersFragment();
                    fragment.setArguments(bundle);
                }
                dialog.dismiss();
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .commit();

            }

        }
    }
    }

