package com.example.hermesbetav2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.hermesbetav2.model.ChatModel;
import com.example.hermesbetav2.util.ChatListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private EditText sendMessageText;
    private ImageButton sendMessageButton;
    private RecyclerView listOfMessages;
    private String currentCommunity;
    private List<ChatModel> chatList;
    private ChatListAdapter adapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: Update activity_chat.xml and make required UI changes
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        currentCommunity = getIntent().getStringExtra("community");
        sendMessageText = findViewById(R.id.sendMessageText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        listOfMessages = findViewById(R.id.listOfMessages);

        //TODO: Set up RecyclerView Adapter and instantiate here to display messages


        db.collection("Messages")
                .whereEqualTo("community", currentCommunity)
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.d(TAG, "Error in collecting messages: " + e.getMessage());
                        } else {

                            if(!queryDocumentSnapshots.isEmpty()) {

                                //TODO: Try to limit creation of so many objects

                                chatList = new ArrayList<>();
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    ChatModel chatModel = snapshot.toObject(ChatModel.class);
                                    chatList.add(chatModel);
                                }

                                listOfMessages.setHasFixedSize(true);
                                listOfMessages.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                                adapter = new ChatListAdapter(ChatActivity.this, chatList);
                                listOfMessages.setAdapter(adapter);
                            }
                        }
                    }
                });

        //TODO: Set OnClickListener to send messages ie store to FirebaseFirestore

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = sendMessageText.getText().toString().trim();
                if(!text.equals("")){
                    ChatModel chatModel = new ChatModel(text, user.getDisplayName(), user.getUid(), currentCommunity, new Timestamp(new Date()));
                    db.collection("Messages")
                            .add(chatModel)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    sendMessageText.setText("");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChatActivity.this, "Failed! Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

//
                }

            }
        });
    }


}
