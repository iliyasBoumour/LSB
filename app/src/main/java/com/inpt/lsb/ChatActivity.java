package com.inpt.lsb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inpt.Util.CurrentUserInfo;
import com.inpt.adapters.MessageAdapter;
import com.inpt.models.MessageModel;

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
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReferenceMsg = db.collection("Messages");
    List<String> ids = new ArrayList<>();
    List<MessageModel> messages;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            MessageModel message = new MessageModel(currentUserId, userId, msgField.getText().toString().trim());
            sendMessage(message);
            msgField.setText("");
        }

    }

    private void sendMessage(MessageModel message) {
        databaseReference.push().setValue(message);
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
                    if((message.getReceiverId().contentEquals(currentUserId) && message.getSenderId().contentEquals(userId))
                                || (message.getReceiverId().contentEquals(userId) && message.getSenderId().contentEquals(currentUserId))) {
                        messages.add(message);

                    }
                }
                messageAdapter = new MessageAdapter(getApplicationContext(), messages, pdpUrl);
                recyclerView.setAdapter(messageAdapter);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}