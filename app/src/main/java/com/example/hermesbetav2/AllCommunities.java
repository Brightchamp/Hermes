package com.example.hermesbetav2;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.hermesbetav2.model.CommunityModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllCommunities extends ListFragment {

    private static final String TAG = "AllCommunities";
    private List<String> allCommunities;
    private ArrayAdapter<String> adapter;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private Button joinCommunityOk, joinCommunityNo;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private FirebaseUser user;

    public AllCommunities() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        db.collection("List of Communities")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null){
                            Log.d(TAG, "onEvent: Error obtaining communities");
                        } else {

                            allCommunities = new ArrayList<>();
                            assert queryDocumentSnapshots != null;
                            if(!queryDocumentSnapshots.isEmpty()){

                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                    CommunityModel communityModel = snapshot.toObject(CommunityModel.class);
                                    allCommunities.add(communityModel.getName());
                                }

                            }

                            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, allCommunities);
                            setListAdapter(adapter);
                        }

                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_communities, container, false);

        return view;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, final int position, long id) {

        builder = new AlertDialog.Builder(getActivity());
        View popupView = getLayoutInflater().inflate(R.layout.popup_join_community, null);
        joinCommunityOk = popupView.findViewById(R.id.joinCommunityOk);
        joinCommunityNo = popupView.findViewById(R.id.joinCommunityNo);
        builder.setView(popupView);
        dialog = builder.create();
        dialog.show();


        joinCommunityOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Add this community to the user's list of communities, and transition to the chat class

                final String communityName = allCommunities.get(position);
                db.collection("Users")
                        .whereEqualTo("userId", user.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        DocumentReference doc = snapshot.getReference();
                                        doc.update("userCommunities", FieldValue.arrayUnion(communityName));
                                    }
                                }

                                dialog.dismiss();

                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("community", communityName);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Couldn't join this community");
                                dialog.dismiss();
                            }
                        });
            }
        });


        joinCommunityNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


}
