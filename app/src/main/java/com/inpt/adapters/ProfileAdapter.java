package com.inpt.adapters;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.HomeFragment;
import com.inpt.lsb.PostFragment;
import com.inpt.lsb.R;
import com.inpt.models.Post;
import com.squareup.picasso.Picasso;

import java.util.List;



public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private FragmentManager fragmentManager;


    public ProfileAdapter(Context context, List<Post> posts, FragmentManager fragmentManager) {
        this.context = context;
        this.posts = posts;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_profile, parent, false);

        return new ViewHolder(view, context, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        String imageUrl;
        imageUrl = post.getImageUrl();
        /*Log.d("TEST", "onBindViewHolder: " + imageUrl);
        Picasso.get().load(imageUrl).placeholder(R.drawable.logo).fit().into(holder.postImage);*/
        Uri uri = Uri.parse(imageUrl);
        holder.postImage.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView postImage;
        FragmentManager fragmentManager;
        Fragment fragment;

        public ViewHolder(@NonNull final View itemView, final Context ctx, final FragmentManager f) {
            super(itemView);
            context = ctx;
            fragmentManager = f;

            postImage = (SimpleDraweeView) itemView.findViewById(R.id.postImage);

            postImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
/*
                    (HideBottomViewOnScrollBehavior)((DashboardActivity)context).findViewById(R.id.bottomBar).
*/
                    String imageUrl = posts.get(getAdapterPosition()).getImageUrl();
                    String caption = posts.get(getAdapterPosition()).getCaption();
                    String time = (String) DateUtils.getRelativeTimeSpanString(posts.get(getAdapterPosition()).getTimeAdded().getSeconds() * 1000);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageUrl", imageUrl);
                    bundle.putString("caption", caption);
                    bundle.putString("time", time);
                    fragmentManager = f;
                    fragment = fragmentManager.findFragmentById(R.id.homeFragment);
                    fragment = new PostFragment();
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                                .replace(R.id.homeFragment, fragment)
                                .commit();

                }
            });
        }
    }
}
