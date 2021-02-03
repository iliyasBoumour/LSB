package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.ChatAdapter;
import com.inpt.adapters.MessagesAdapter;
import com.inpt.models.LastMessageModel;
import com.inpt.models.MessageModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessagesActivity extends AppCompatActivity implements View.OnClickListener {

    private List<LastMessageModel> lastMessageModels;
    private Map<String, MessageObject> hashMap;
    private RecyclerView recyclerView;
    private MessagesAdapter messagesAdapter;
    private String currentUserId;
    private ImageView back;
    private ProgressBar progressBar;
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        currentUserId = CurrentUserInfo.getInstance().getUserId();
        progressBar = findViewById(R.id.progressBar);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        recyclerView = findViewById(R.id.messages_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        hashMap = new HashMap();
        lastMessageModels = new ArrayList<>();
        getLastMessages();



    }

    private void getLastMessages() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hashMap.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
                    if(message != null) {
                        if(message.getSenderId().contentEquals(currentUserId) && !hashMap.containsKey(message.getReceiverId())) {
                            hashMap.put(message.getReceiverId(), new MessageObject(message.getMessage(), message.getTime()));
                        } else if(message.getReceiverId().contentEquals(currentUserId) && !hashMap.containsKey(message.getSenderId())) {
                            hashMap.put(message.getSenderId(), new MessageObject(message.getMessage(), message.getTime()));
                        }else {
                            if(message.getSenderId().contentEquals(currentUserId) &&hashMap.containsKey(message.getReceiverId())) {
                                hashMap.remove(message.getReceiverId());
                                hashMap.put(message.getReceiverId(), new MessageObject(message.getMessage(), message.getTime()));
                            }else if(message.getReceiverId().contentEquals(currentUserId) &&hashMap.containsKey(message.getSenderId())) {
                                hashMap.remove(message.getSenderId());
                                hashMap.put(message.getSenderId(), new MessageObject(message.getMessage(), message.getTime()));
                            }
                        }

                    }

                }
                getUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);

            }
        });
    }

    private void getUsers() {
        progressBar.setVisibility(View.INVISIBLE);
        lastMessageModels.clear();
        for(String id : hashMap.keySet()) {
            collectionReferenceUsers.whereEqualTo("uid", id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                                LastMessageModel lastMessageModel = new LastMessageModel();
                                lastMessageModel.setMessage(hashMap.get(id).getMessage());
                                lastMessageModel.setPdpUrl(userDocument.getString("pdp"));
                                lastMessageModel.setUserName(userDocument.getString("username"));
                                lastMessageModel.setTime(hashMap.get(id).getTime());
                                lastMessageModel.setUserId(userDocument.getString("uid"));
                                lastMessageModels.add(lastMessageModel);
                            }
                            messagesAdapter = new MessagesAdapter(getApplicationContext(), lastMessageModels);
                            recyclerView.setAdapter(messagesAdapter);
                            messagesAdapter.notifyDataSetChanged();

                        }
                    });

        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back) {
            finish();
        }
    }

    public class MessageObject {
        private String message;
        private long time;

        public MessageObject(String message, long time) {
            this.message = message;
            this.time = time;
        }

        public MessageObject() {
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }

    private void setStatus(String status) {
        collectionReferenceUsers.whereEqualTo("uid", currentUserInfo.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", status);
                        for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                            Log.d("STATUS", "onSuccess: ");
                            userDocument.getReference().update(data);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }
}