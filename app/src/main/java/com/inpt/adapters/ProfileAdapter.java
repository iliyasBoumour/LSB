package com.inpt.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.inpt.lsb.DashboardActivity;
import com.inpt.lsb.PostFragment;
import com.inpt.lsb.R;
import com.inpt.models.Post;

import java.util.List;



public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private FragmentManager fragmentManager;
    private String userName;
    private String pdpUrl;


    public ProfileAdapter(Context context, List<Post> posts, FragmentManager fragmentManager, String userName, String pdpUrl) {
        this.context = context;
        this.posts = posts;
        this.fragmentManager = fragmentManager;
        this.userName = userName;
        this.pdpUrl = pdpUrl;
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

                    String postId = posts.get(getAdapterPosition()).getPostId();
                    String userId = posts.get(getAdapterPosition()).getUserId();
                    Bundle bundle = new Bundle();
                    bundle.putString("postId", postId);
                    bundle.putString("userName", userName);
                    bundle.putString("pdpUrl", pdpUrl);
                    bundle.putString("userId", userId);
                    fragmentManager = f;
                    fragment = fragmentManager.findFragmentById(R.id.homeFragment);
                    fragment = new PostFragment();
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                                .replace(R.id.homeFragment, fragment)
                            .addToBackStack(null)
                                .commit();

                }
            });
        }
    }
}
