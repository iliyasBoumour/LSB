package com.inpt.lsb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.Util.SendNotif;
import com.inpt.adapters.ChatAdapter;
import com.inpt.models.MessageModel;
import com.inpt.models.NotificationModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleDraweeView pdp;
    private EditText msgField;
    private ImageView sendBtn, backBtn;
    private TextView userNameTextView;
    private String userName, pdpUrl, userId, currentUserId;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
    List<String> ids = new ArrayList<>();
    List<MessageModel> messages;
    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    SendNotif sendNotif;
    private static final String NOTIF_MESSAGE="message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendNotification(false);
        setContentView(R.layout.activity_chat);
        currentUserId = CurrentUserInfo.getInstance().getUserId();
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

    private void sendNotification(Boolean send) {
        SharedPreferences.Editor editor = getSharedPreferences("NOTIF", MODE_PRIVATE).edit();
        editor.putBoolean("send", send);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendNotification(false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        logd
        sendNotification(true);
    }


    private void sendMessage(MessageModel message) {
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
}