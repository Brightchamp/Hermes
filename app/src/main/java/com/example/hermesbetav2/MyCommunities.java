package com.example.hermesbetav2;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hermesbetav2.model.CommunityModel;
import com.example.hermesbetav2.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyCommunities extends Fragment {

    private static final String TAG = "MyCommunities";
    private ListView listView;
    private FloatingActionButton addCommunities;
    private List<String> userCommunities;
    private ArrayAdapter<String> adapter;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;

    public MyCommunities() {
        // Required empty public constructor
    }


    /**
     *
     * @param inflater to inflate our xml file
     * @param container container
     * @param savedInstanceState unused
     * @return the required view
     *
     * The function returns the required fragment view, after performing the requisite CRUD operations in Firestore
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_communities, container, false);


        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        listView = view.findViewById(R.id.myCommunityList);
        addCommunities = view.findViewById(R.id.addCommunities);

        addCommunities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Show Alert Dialog and get details from user

                builder = new AlertDialog.Builder(getContext());
                View popView = getLayoutInflater().inflate(R.layout.popup_add_community, null);
                final EditText nameOfCommunity = popView.findViewById(R.id.nameOfCommunity);
                Button addCommunitySave = popView.findViewById(R.id.addCommunitySave);

                builder.setView(popView);
                dialog = builder.create();
                dialog.show();

                //TODO: Add the new Community to the required collection with the requisite details and include in current user's list of communities

                addCommunitySave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        assert user != null;
                        final CommunityModel communityModel = new CommunityModel(user.getDisplayName(), nameOfCommunity.getText().toString().trim());
                        db.collection("List of Communities")
                                .add(communityModel)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        dialog.dismiss();

                                        db.collection("Users")
                                                .whereEqualTo("userId", user.getUid())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if(!queryDocumentSnapshots.isEmpty()){
                                                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                                                DocumentReference doc = snapshot.getReference();
                                                                doc.update("userCommunities", FieldValue.arrayUnion(communityModel.getName()));
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Log.d(TAG, "Adding new communities. Failed: " + e.getMessage());
                                    }
                                });
                    }
                });
            }
        });


        //TODO: Get all communities the user is a part of and display them


//        db.collection("Users")
//                .whereEqualTo("userID", user.getUid())
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                        assert queryDocumentSnapshots != null;
//                        if(!queryDocumentSnapshots.isEmpty()) {
//
//                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
//                                UserModel userModel = snapshot.toObject(UserModel.class);
//                                userCommunities = userModel.getUserCommunities();
//                            }
//
//                        }
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "Failed to get document");
//                    }
//                });


        try{
            assert user != null;
            db.collection("Users")
                    .whereEqualTo("userId", user.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.d(TAG, "Collecting my communities. Failed: " + e.getMessage());
                            } else {

                                assert queryDocumentSnapshots != null;
                                if(!queryDocumentSnapshots.isEmpty()) {

                                    //Log.d(TAG, "onEvent: Pretesting");
                                    userCommunities = new ArrayList<>();
                                    for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                        UserModel userModel = snapshot.toObject(UserModel.class);
                                        userCommunities = userModel.getUserCommunities();
                                        //Log.d(TAG, "onEvent: Collecting communities " + userModel);
                                    }

                                }

                                if(userCommunities != null){
                                    adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, userCommunities);
                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }

                                //Log.d(TAG, "onEvent: Collected communities and displayed\n" + userCommunities);

                            }

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: Set on Item click Listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String community = userCommunities.get(position);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("community", community);
                startActivity(intent);
            }
        });

        return view;
    }

}
