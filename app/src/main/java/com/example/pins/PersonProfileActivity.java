package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {


    private TextView displayName, fullName, gender;
    private DatabaseReference userRef, friendRequestRef, friendsRef, blockedRef;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID;
    private Button declineBtn, sendBtn, blockBtn, unblockBtn;
    private String CURRENT_STATE, dateBefriended, dateBlocked, BLOCKED_STATE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friendlists");
        blockedRef = FirebaseDatabase.getInstance().getReference().child("Blocked");

        InitFields(); // TODO: every activity should be refactored like <- this

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String MyUserName = dataSnapshot.child("display_name").getValue().toString();
                    String MyFullName = dataSnapshot.child("full_name").getValue().toString();
                    String MyGender = dataSnapshot.child("gender").getValue().toString();

                    displayName.setText("@" + MyUserName);
                    fullName.setText(MyFullName);
                    gender.setText(MyGender);


                    MaintainUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        if(senderUserID.equals(receiverUserID)){
            sendBtn.setVisibility(View.INVISIBLE);
            sendBtn.setEnabled(false);

            declineBtn.setVisibility(View.INVISIBLE);
            declineBtn.setEnabled(false);

            blockBtn.setVisibility(View.INVISIBLE);
            blockBtn.setEnabled(false);

            unblockBtn.setVisibility(View.INVISIBLE);
            unblockBtn.setEnabled(false);


        } else {
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendBtn.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){

                        declineBtn.setVisibility(View.INVISIBLE);
                        declineBtn.setEnabled(false);

                        SendFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_sent")){

                        declineBtn.setVisibility(View.INVISIBLE);
                        declineBtn.setEnabled(false);

                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){

                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){

                        Unfriend();
                    }
                }
            });
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Block();
                }
            });
            unblockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Unblock();
                }
            });


        }
    }
    private void Block(){
        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        dateBlocked = currentDate.format(date.getTime());
        blockedRef.child(senderUserID).child(receiverUserID)
                .setValue(dateBlocked);
        unblockBtn.setVisibility(View.VISIBLE);
        unblockBtn.setEnabled(true);
    }
    private void Unblock(){
        blockedRef.child(senderUserID).child(receiverUserID).removeValue();
        unblockBtn.setVisibility(View.INVISIBLE);
        unblockBtn.setEnabled(false);
    }


    private void Unfriend() {
        friendsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendBtn.setEnabled(true); //true?
                                                CURRENT_STATE = "not_friends";
                                                sendBtn.setText("Send Friend Request");

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        dateBefriended = currentDate.format(date.getTime());

        friendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(dateBefriended)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID).child("date").setValue(dateBefriended)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendBtn.setEnabled(true); //true?
                                                                                        CURRENT_STATE = "friends";
                                                                                        sendBtn.setText("Unfriend");

                                                                                        declineBtn.setVisibility(View.INVISIBLE);
                                                                                        declineBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                //CURRENT_STATE = "friends";
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendRequestRef.child(receiverUserID).child(senderUserID)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendBtn.setEnabled(true); //true?
                                CURRENT_STATE = "not_friends";
                                sendBtn.setText("Send Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void MaintainUI() {
        friendRequestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        sendBtn.setText("Cancel Friend Request");

                        declineBtn.setVisibility(View.INVISIBLE);
                        declineBtn.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        CURRENT_STATE = "request_received";

                        sendBtn.setText("Accept Friend Request");

                        declineBtn.setVisibility(View.VISIBLE);
                        declineBtn.setEnabled(true);

                        declineBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });
                    }

                }
                else {
                    friendsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserID)){
                                CURRENT_STATE = "friends";
                                sendBtn.setText("Unfriend");
                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        blockedRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    blockBtn.setVisibility(View.INVISIBLE);
                    blockBtn.setEnabled(false);
                    unblockBtn.setVisibility(View.VISIBLE);
                    unblockBtn.setEnabled(true);
                }
                else {
                    unblockBtn.setVisibility(View.INVISIBLE);
                    unblockBtn.setEnabled(false);
                    blockBtn.setVisibility(View.VISIBLE);
                    blockBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendRequestRef.child(receiverUserID).child(senderUserID)
                            .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendBtn.setEnabled(true); //true?
                                CURRENT_STATE = "request_sent";
                                sendBtn.setText("Cancel Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void InitFields() {
        displayName = findViewById(R.id.personDisplayName);
        fullName = findViewById(R.id.personFullname);
        gender = findViewById(R.id.personGender);

        sendBtn = findViewById(R.id.personSendRequestBtn);

        declineBtn = findViewById(R.id.personDeclineBtn);


        blockBtn = findViewById(R.id.personBlockBtn);

        unblockBtn = findViewById(R.id.personUblockBtn);

        CURRENT_STATE = "not_friends";// should change i think


    }


}
