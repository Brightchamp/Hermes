package com.example.hermesbetav2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.hermesbetav2.model.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SignInActivity extends FragmentActivity {

    private static final int RC_SIGN_IN = 7777;
    private static final String TAG = "HermesMain";
    private GoogleSignInClient googleSignInClient;
    private SignInButton signInButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference users = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        /* Configure sign-in to request the user's ID, email address, and basic
            profile. ID and basic profile are included in DEFAULT_SIGN_IN.
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        //building a sign in client with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        signInButton = findViewById(R.id.signinButton);
        // Set the dimensions of the sign-in button
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //TODO: Add Progress Dialog
    }


    /**
     * Gets signInIntent() and then starts activity for result
     */
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     * @param requestCode specified by the user
     * @param resultCode
     * @param data required data
     *
     * If our request code matches, we then obtain our task (which attempted to sign in to the account)
     * We surround with a try-catch block, and store our required Google Account, then call another method to obtain Firebase credentials
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //now we shall check whether user is from iith
                String email = account.getEmail();
                if(email.length() > 10 && email.substring(email.length() - 10).compareTo("iith.ac.in") == 0) {
                    firebaseAuthWithGoogle(account);
                }

                else {
                    Snackbar.make(findViewById(R.id.main_layout), "User is not using IIT-H account", Snackbar.LENGTH_SHORT).show();
                    googleSignInClient.signOut()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                }

            } catch (ApiException e) {

                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "Google sign in failed", e);
                updateUI(null);

            }
        }
    }


    /**
     *
     * @param account the signed in account
     * We obtain the necessary Firebase credentials, and sign in the user with those credentials
     * If the sign in is successful, we update the UI accordingly
     */
    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(account.getDisplayName()).build();
                            user.updateProfile(profileUpdates);
                            final UserModel userModel = new UserModel(user.getUid(), account.getDisplayName());
                            users.whereEqualTo("userId", user.getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if(!queryDocumentSnapshots.isEmpty()){
                                                Log.d(TAG, "queryDocumentSnapshots != null");
                                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                                    Log.d(TAG, "Users: " + snapshot.toObject(UserModel.class).getUserName());
                                                }

                                            } else {
                                                users.add(userModel)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.d(TAG, "Added user successfully");

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "onFailure: Document creation failed");
                                                            }
                                                        });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Failed to even get document");
                                        }
                                    });
//                            users.add(userModel)
//                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                        @Override
//                                        public void onSuccess(DocumentReference documentReference) {
//
//
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d(TAG, "onFailure: Document creation failed");
//                                        }
//                                    });
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();

        //check if user exists and update accordingly
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);

    }


    /**
     *
     * @param user
     *
     * if user is null, make the sign in button visible
     * Else, make the button invisible, and then start new activity, and finish current activity
     */
    private void updateUI(FirebaseUser user) {

        if(user == null){
            signInButton.setVisibility(View.VISIBLE);
        }

        else {
            signInButton.setVisibility(View.INVISIBLE);
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }

    }
}
