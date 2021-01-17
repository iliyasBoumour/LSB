package com.inpt.adapters;

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
import com.inpt.lsb.R;
import com.inpt.models.SearchResModel;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>  {

    private List<SearchResModel> searchResModels=new ArrayList<>();
    private Context context;

    public SearchAdapter(Context context){
        this.context=context;
    }


    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.search_res_item,parent,false);
        SearchAdapter.ViewHolder holder=new SearchAdapter.ViewHolder(view,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.name.setText(searchResModels.get(position).getName());
        holder.pdp.setImageResource(R.drawable.pdp);
//        holder.item.setOnClickListener(view -> {
//            Toast.makeText(context, notifications.get(position).getNotification(), Toast.LENGTH_SHORT).show();
//        });
    }

    public void setSearchResModels(List<SearchResModel> searchResModels) {
        this.searchResModels = searchResModels;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return searchResModels.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        //in our contact_item_layout we had a TextView and a relativeLayout so we have to definet them here
        private ConstraintLayout item;
        private ImageView pdp;
        private TextView name;
        public ViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            pdp=itemView.findViewById(R.id.profile);
            name=itemView.findViewById(R.id.name);
        }
    }
}