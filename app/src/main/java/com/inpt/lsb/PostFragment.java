package com.inpt.lsb;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;

import java.util.HashMap;
import java.util.Map;


public class PostFragment extends Fragment implements View.OnClickListener, GestureDetector.OnGestureListener, View.OnTouchListener{
    private static final float SWIPE_THRESHOLD = 100;
    private static final float SWIPE_VELOCITY_THRESHOLD = 100;
    private SimpleDraweeView simpleDraweeView;
    private TextView captionTextView, timeTextView, likesTextView;
    private ImageView like_icone;
    private String currentUserId;
    private String postId;
    private Boolean isliked;
    private int nbLike;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Likes");
    private CollectionReference collectionReferencePost = db.collection("Posts");
    public static final String TAG = "EVENT";
    private GestureDetector gestureDetector;


    public PostFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = this.getArguments().getString("postId");
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        Log.d("UID", "onCreate: " + currentUserId);
        Log.d("PID", "onCreate: " + postId);
        gestureDetector = new GestureDetector(getActivity().getApplicationContext(), this);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_fragment, container, false);
        simpleDraweeView = view.findViewById(R.id.postImage);
        captionTextView = view.findViewById(R.id.caption);
        likesTextView = view.findViewById(R.id.likes_nb);
        like_icone = view.findViewById(R.id.like);
        timeTextView = view.findViewById(R.id.time);
        Bundle bundle = this.getArguments();
        String imageUrl = bundle.getString("imageUrl");
        String caption = bundle.getString("caption");
        String time = bundle.getString("time");
        nbLike = bundle.getInt("nbLike");
        likesTextView.setText(setnbLike(nbLike));
        captionTextView.setText(caption);
        timeTextView.setText(time);
        Uri uri = Uri.parse(imageUrl);
        simpleDraweeView.setImageURI(uri);
        like_icone.setOnClickListener(this);
        simpleDraweeView.setOnTouchListener(this);
        checkReact();

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.like:
                React();
                break;
        }
    }

    private void React() {
        if(isliked) {
            like_icone.setEnabled(false);
            collectionReference.document(currentUserId + "_" + postId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            nbLike -= 1;
                            collectionReferencePost.whereEqualTo("postId", postId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("nbLike", nbLike);
                                                document.getReference().update(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                like_icone.setImageResource(R.drawable.ic_before);
                                                                like_icone.setEnabled(true);
                                                                isliked = false;
                                                                likesTextView.setText(setnbLike(nbLike));
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    });

        } else {
            Map<String, String> data = new HashMap<>();
            data.put("userId", currentUserId);
            data.put("postId", postId);
            like_icone.setEnabled(false);
            collectionReference.document(currentUserId + "_" + postId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            nbLike += 1;
                            collectionReferencePost.whereEqualTo("postId", postId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("nbLike", nbLike);
                                                document.getReference().update(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                like_icone.setImageResource(R.drawable.ic_after);
                                                                like_icone.setEnabled(true);
                                                                isliked = true;
                                                                likesTextView.setText(setnbLike(nbLike));
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    });
        }


    }
    private void checkReact() {
        collectionReference.document(currentUserId + "_" + postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            like_icone.setImageResource(R.drawable.ic_after);
                            isliked = true;
                        } else {
                            like_icone.setImageResource(R.drawable.ic_before);
                            isliked = false;
                        }
                    }
                });
    }

    private String setnbLike(int nb) {
        String str = "";
        if(nb > 1) {
            str = nb + "Likes";
        } else if (nb == 1) {
            str = nb + "Like";
        }
        return str;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
        boolean result = false;
        float diffY = moveEvent.getY() - downEvent.getY();
        float diffX = moveEvent.getX() - downEvent.getX();
        // which was greater?  movement across Y or X?
        if (Math.abs(diffX) > Math.abs(diffY)) {
            // right or left swipe
            if (Math.abs(diffX)> SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeRight();
                } else {
                   /* onSwipeLeft();*/
                }
                result = true;
            }
        } else {
            // up or down swipe
            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY)> SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                } else {
                  
                }
                result = true;
            }
        }

        return result;
    }

    private void onSwipeRight() {
        if(!isliked) {
            Map<String, String> data = new HashMap<>();
            data.put("userId", currentUserId);
            data.put("postId", postId);
            like_icone.setEnabled(false);
            collectionReference.document(currentUserId + "_" + postId)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            nbLike += 1;
                            collectionReferencePost.whereEqualTo("postId", postId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("nbLike", nbLike);
                                                document.getReference().update(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                like_icone.setImageResource(R.drawable.ic_after);
                                                                like_icone.setEnabled(true);
                                                                isliked = true;
                                                                likesTextView.setText(setnbLike(nbLike));
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    });
        }
        Log.d(TAG, "onSwipeRight: ");
        Toast.makeText(getActivity(), "Swipe Right", Toast.LENGTH_SHORT).show();

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }
}
