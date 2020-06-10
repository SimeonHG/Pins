package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {


    private TextView displayName, fullName, gender, score;
    private ImageView profilePic;

    private DatabaseReference userRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendsRef;
    private DatabaseReference blockedRef;
    private DatabaseReference scoresRef;

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
        scoresRef = FirebaseDatabase.getInstance().getReference().child("Scores");

        initFields();
        setScore();



        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userName = dataSnapshot.child("display_name").getValue().toString();
                    String userFullName = dataSnapshot.child("full_name").getValue().toString();
                    String userGender = dataSnapshot.child("gender").getValue().toString();
                    Picasso.get().load(R.drawable.genericpic);

                    if(dataSnapshot.hasChild("profilePic")){
                        String image = dataSnapshot.child("profilePic").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.genericpic).into(profilePic);
                    }

                    displayName.setText("@" + userName);
                    fullName.setText(userFullName);
                    gender.setText(userGender);

                    maintainUI();
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

                            sendFriendRequest();
                            break;
                        case REQUEST_SENT:
                            declineBtn.setVisibility(View.INVISIBLE);
                            declineBtn.setEnabled(false);

                            cancelFriendRequest();
                            break;
                        case REQUEST_RECEIVED:
                            acceptFriendRequest();
                            break;
                        case FRIENDS:
                            unfriend();
                            break;
                    }
                }
            });
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block();
                }
            });
            unblockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unblock();
                }
            });


        }
    }

    private void setScore() {
        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(senderUserID).exists() && dataSnapshot.child(senderUserID).hasChild(receiverUserID)){
                    score.setText("Score = " + dataSnapshot.child(senderUserID).child(receiverUserID).getValue().toString());
                }
                else {
                    score.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void block(){
        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        dateBlocked = currentDate.format(date.getTime());
        blockedRef.child(senderUserID).child(receiverUserID).child("date").setValue(dateBlocked);
        unblockBtn.setVisibility(View.VISIBLE);
        unblockBtn.setEnabled(true);
    }
    private void unblock(){
        blockedRef.child(senderUserID).child(receiverUserID).removeValue();
        unblockBtn.setVisibility(View.INVISIBLE);
        unblockBtn.setEnabled(false);
    }


    private void unfriend() {
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
                                                sendBtn.setEnabled(true);
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

    private void acceptFriendRequest() {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
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
                                                                                        sendBtn.setEnabled(true);
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

    private void cancelFriendRequest() {
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

    private void maintainUI() {
        friendRequestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        friendshipStatus = FriendshipStatus.REQUEST_SENT;
                        sendBtn.setText("Cancel");

                        declineBtn.setVisibility(View.INVISIBLE);
                        declineBtn.setEnabled(false);
                    }
                    else if(request_type.equals("received")){
                        friendshipStatus = FriendshipStatus.REQUEST_RECEIVED;
                        sendBtn.setText("Accept");

                        declineBtn.setVisibility(View.VISIBLE);
                        declineBtn.setEnabled(true);

                        declineBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
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

    private void sendFriendRequest() {
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
                                sendBtn.setText("Cancel");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void initFields() {
        displayName = findViewById(R.id.personDisplayName);
        fullName = findViewById(R.id.personFullname);
        gender = findViewById(R.id.personGender);
        score = findViewById(R.id.score);

        profilePic = findViewById(R.id.personProfilePic);

        sendBtn = findViewById(R.id.personSendRequestBtn);

        declineBtn = findViewById(R.id.personDeclineBtn);


        blockBtn = findViewById(R.id.personBlockBtn);

        unblockBtn = findViewById(R.id.personUblockBtn);

        friendshipStatus = FriendshipStatus.NOT_FRIENDS;
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);


    }


}
