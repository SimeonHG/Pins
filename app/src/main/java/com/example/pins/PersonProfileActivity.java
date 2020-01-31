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
    private DatabaseReference userRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendsRef;
    private DatabaseReference blockedRef;

    private FirebaseAuth mAuth;
    private String senderUserID;
    private String receiverUserID;
    private Button declineBtn;
    private Button sendBtn;
    private Button blockBtn;
    private Button unblockBtn;

    private FriendshipStatus friendshipStatus;
    private String dateBefriended;
    private String dateBlocked;

    private enum FriendshipStatus {
        NOT_FRIENDS,
        FRIENDS,
        REQUEST_SENT,
        REQUEST_RECEIVED
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friendlists");
        blockedRef = FirebaseDatabase.getInstance().getReference().child("Blocked");

        InitFields(); // TODO: every activity should be refactored like <- this
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userName = dataSnapshot.child("display_name").getValue().toString();
                    String userFullName = dataSnapshot.child("full_name").getValue().toString();
                    String userGender = dataSnapshot.child("gender").getValue().toString();

                    displayName.setText("@" + userName);
                    fullName.setText(userFullName);
                    gender.setText(userGender);

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
                    switch (friendshipStatus) {
                        case NOT_FRIENDS:
                            declineBtn.setVisibility(View.INVISIBLE);
                            declineBtn.setEnabled(false);

                            SendFriendRequest();
                            break;
                        case REQUEST_SENT:
                            declineBtn.setVisibility(View.INVISIBLE);
                            declineBtn.setEnabled(false);

                            CancelFriendRequest();
                            break;
                        case REQUEST_RECEIVED:
                            AcceptFriendRequest();
                            break;
                        case FRIENDS:
                            Unfriend();
                            break;
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
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        dateBlocked = currentDate.format(date.getTime());
        blockedRef.child(senderUserID).child(receiverUserID).setValue(dateBlocked);
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
                                                friendshipStatus = FriendshipStatus.NOT_FRIENDS;
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
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
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
                                                                                        friendshipStatus = FriendshipStatus.FRIENDS;
                                                                                        sendBtn.setText("Unfriend");

                                                                                        declineBtn.setVisibility(View.INVISIBLE);
                                                                                        declineBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

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
                                friendshipStatus = FriendshipStatus.NOT_FRIENDS;
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
                        friendshipStatus = FriendshipStatus.REQUEST_SENT;
                        sendBtn.setText("Cancel Friend Request");

                        declineBtn.setVisibility(View.INVISIBLE);
                        declineBtn.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        friendshipStatus = FriendshipStatus.REQUEST_RECEIVED;
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
                                friendshipStatus = FriendshipStatus.FRIENDS;
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
                else if(!senderUserID.equals(receiverUserID)){
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
                                friendshipStatus = FriendshipStatus.REQUEST_SENT;
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

        friendshipStatus = FriendshipStatus.NOT_FRIENDS;// should change i think
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);


    }


}
