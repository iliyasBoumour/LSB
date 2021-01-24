package com.inpt.adapters;

import android.content.Context;
import android.os.Bundle;
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
import com.inpt.Util.CurrentUserInfo;
import com.inpt.lsb.ProfileCurrentUserFragment;
import com.inpt.lsb.ProfileOtherUsersFragment;
import com.inpt.lsb.R;
import com.inpt.models.SearchResModel;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<SearchResModel> searchResModels = new ArrayList<>();
    private Context context;
    private FragmentManager fragmentManager;


    public SearchAdapter(Context context) {
        this.context = context;
    }

    public SearchAdapter(Context context, List<SearchResModel> searchResModels, FragmentManager fragmentManager) {
        this.context = context;
        this.searchResModels = searchResModels;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_res_item, parent, false);
        SearchAdapter.ViewHolder holder = new SearchAdapter.ViewHolder(view, context, fragmentManager);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.name.setText(searchResModels.get(position).getName());
        holder.pdp.setImageURI(searchResModels.get(position).getImage());
    }

    public void setSearchResModels(List<SearchResModel> searchResModels) {
        this.searchResModels = searchResModels;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return searchResModels.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ConstraintLayout item;
        private SimpleDraweeView pdp;
        private TextView name;
        FragmentManager fragmentManager;
        Fragment fragment;

        public ViewHolder(@NonNull View itemView, Context ctx, final FragmentManager f) {
            super(itemView);
            fragmentManager = f;
            context = ctx;


            item = itemView.findViewById(R.id.item);
            pdp = itemView.findViewById(R.id.profile);
            name = itemView.findViewById(R.id.name);

            item.setOnClickListener(this);
            pdp.setOnClickListener(this);
            name.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String userId = searchResModels.get(getAdapterPosition()).getId();
            String currentUserId = CurrentUserInfo.getInstance().getUserId();
            String userName = searchResModels.get(getAdapterPosition()).getName();
            String pdpUrl = searchResModels.get(getAdapterPosition()).getImage();

            if(v.getId() == R.id.item || v.getId() == R.id.profile || v.getId() == R.id.name) {
                fragment = fragmentManager.findFragmentById(R.id.homeFragment);
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("userName", userName);
                bundle.putString("pdpUrl", pdpUrl);
                if(userId.contentEquals(currentUserId)) {
                    fragment = new ProfileCurrentUserFragment();
                } else  {
                    fragment = new ProfileOtherUsersFragment();
                    fragment.setArguments(bundle);
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.homeFragment, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

    }
}