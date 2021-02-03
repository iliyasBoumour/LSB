package com.inpt.lsb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.SendNotif;
import com.inpt.adapters.ChatAdapter;
import com.inpt.models.MessageModel;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleDraweeView pdp;
    private EditText msgField;
    private ImageView sendBtn, backBtn;
    private TextView userNameTextView, statusTextView;
    private String userName, pdpUrl, userId, currentUserId;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
    List<String> ids = new ArrayList<>();
    List<MessageModel> messages;
    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    SendNotif sendNotif;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();
    private CollectionReference collectionReferenceUsers = db.collection("users");
    private static final String NOTIF_MESSAGE="message";
    public static final float ONLINE = 4.0f;
    public static final float OFFLINE = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendNotification(userId);
        setContentView(R.layout.activity_chat);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
        statusTextView = findViewById(R.id.status_TextView);
        recyclerView = findViewById(R.id.chat_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        ids.add(currentUserId);
        ids.add(userId);
        pdp = findViewById(R.id.pdp);
        msgField = findViewById(R.id.msgField);
        sendBtn = findViewById(R.id.sendBtn);
        backBtn = findViewById(R.id.back);
        userNameTextView = findViewById(R.id.userName_textView);
        userNameTextView.setOnClickListener(this);
        pdp.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        userName = bundle.getString("userName");
        pdpUrl = bundle.getString("pdpUrl");
        userId = bundle.getString("userId");
        Uri uri = Uri.parse(pdpUrl);
        pdp.setImageURI(uri);
        userNameTextView.setText(userName);
        messages = new ArrayList<>();
        getMessages();
        collectionReferenceUsers.whereEqualTo("uid", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error == null && value != null) {
                            for(QueryDocumentSnapshot userDocument : value) {
                                if(userDocument.getString("status") != null) {
                                    switch (userDocument.getString("status")) {
                                        case "online":
                                            setStatus(pdp, ONLINE);
                                            statusTextView.setText("Online");
                                            break;
                                        case "offline":
                                            setStatus(pdp, OFFLINE);
                                            statusTextView.setText("Offline");
                                            break;
                                    }
                                }
                            }
                        }
                    }
                });
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
    public void onClick(View v) {
        if(v.getId() == R.id.back) {
            finish();
        } else if(v.getId() == R.id.sendBtn) {
            MessageModel message = new MessageModel(currentUserId, userId, msgField.getText().toString().trim(), new Timestamp(new Date()).getSeconds());
            sendMessage(message);
            msgField.setText("");
        }else if(v.getId() == R.id.pdp || v.getId() == R.id.userName_textView) {
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("pdpUrl", pdpUrl);
            intent.putExtra("userId", userId);
            intent.putExtra("Fragment", "profileOtherUsers");
            startActivity(intent);
        }

    }

    private void sendNotification(String userId_) {
        SharedPreferences.Editor editor = getSharedPreferences("NOTIF", MODE_PRIVATE).edit();
        editor.putString("userId", userId_);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendNotification(userId);
        setStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        sendNotification("");
        setStatus("offline");
    }


    private void sendMessage(MessageModel message) {
        if(message.getMessage().trim().isEmpty()) {
            return;
        }
        databaseReference.push().setValue(message);
        CurrentUserInfo currentUserInfo=CurrentUserInfo.getInstance();
        NotificationModel notification=new NotificationModel(currentUserInfo.getUserName(),currentUserInfo.getPdpUrl(),message.getMessage(),NOTIF_MESSAGE,userId,currentUserId);
        sendNotif=new SendNotif(notification);
        sendNotif.send();
    }

    private void getMessages() {

        /*collectionReferenceMsg.orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                messages.clear();
                if(error == null && value != null) {
                    for(QueryDocumentSnapshot msgDocument : value) {
                        MessageModel message = msgDocument.toObject(MessageModel.class);
                        if((message.getReceiverId().contentEquals(currentUserId) && message.getSenderId().contentEquals(userId))
                                || (message.getReceiverId().contentEquals(userId) && message.getSenderId().contentEquals(currentUserId))) {
                            messages.add(message);
                            Log.d( "MSG", "onEvent: " + message.getMessage());

                        }
                    }
                    messageAdapter = new MessageAdapter(getApplicationContext(), messages);
                    recyclerView.setAdapter(messageAdapter);
                    messageAdapter.notifyDataSetChanged();
                }
            }
        });*/
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
                 if(message != null) {
                     if((message.getReceiverId().contentEquals(currentUserId) && message.getSenderId().contentEquals(userId))
                             || (message.getReceiverId().contentEquals(userId) && message.getSenderId().contentEquals(currentUserId))) {
                         messages.add(message);

                     }
                 }
                }
                chatAdapter = new ChatAdapter(getApplicationContext(), messages, pdpUrl, userName, userId);
                recyclerView.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setStatus(SimpleDraweeView simpleDraweeView, float width) {
        int color = getResources().getColor(R.color.green);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setBorder(color, width);
        roundingParams.setRoundAsCircle(true);
        simpleDraweeView.getHierarchy().setRoundingParams(roundingParams);
    }
}