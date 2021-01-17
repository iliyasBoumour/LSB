package com.inpt.lsb;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.ProfileAdapter;
import com.inpt.models.Post;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class PostFragment extends Fragment {
    private SimpleDraweeView simpleDraweeView;
    private TextView captionTextView, timeTextView;


    public PostFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_fragment, container, false);
        simpleDraweeView = view.findViewById(R.id.postImage);
        captionTextView = view.findViewById(R.id.caption);
        timeTextView = view.findViewById(R.id.time);
        Bundle bundle = this.getArguments();
        String imageUrl = bundle.getString("imageUrl");
        String caption = bundle.getString("caption");
        String time = bundle.getString("time");
        captionTextView.setText(caption);
        timeTextView.setText(time);
        Uri uri = Uri.parse(imageUrl);
        simpleDraweeView.setImageURI(uri);

        return view;
    }


}