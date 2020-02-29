package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendlistActivity extends AppCompatActivity {

    private RecyclerView friendsRecycler;
    private DatabaseReference dbRef;
    private DatabaseReference friendlistRef;
    private DatabaseReference usersRef;
    private String currentUserID;
    private List<String> friends;
    private List<String> usernames;
    private List<String> fullnames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);


        friendsRecycler = findViewById(R.id.friendlist_recycler);
        friendsRecycler.setHasFixedSize(true);
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef = FirebaseDatabase.getInstance().getReference();
        friendlistRef = FirebaseDatabase.getInstance().getReference().child("Friendlists").child(currentUserID);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        displayFriends();
//        friends = new ArrayList<>();
//
//        friendlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    friends.add(snapshot.getKey());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        usernames = new ArrayList<>();
//        fullnames = new ArrayList<>();
//
//        for(String friend : friends){
//                usersRef.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        usernames.add(dataSnapshot.child("display_name").getValue().toString());
//                        fullnames.add(dataSnapshot.child("full_name").getValue().toString());
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
       }


        private void displayFriends(){
            Query searchFriends = usersRef.orderByKey();
            FirebaseRecyclerAdapter<User, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendsViewHolder>
                    (
                            User.class, R.layout.all_users_display_layout, FriendsViewHolder.class, searchFriends
                    ) {
                @Override
                protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final User user, final int position) {

                    friendlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user.uid)) {
                                friendsViewHolder.displayUser(user);
                                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String visit_user_id = getRef(position).getKey();

                                        Intent profileIntent = new Intent(FriendlistActivity.this, PersonProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", visit_user_id);
                                        startActivity(profileIntent);
                                    }
                                });
                            }
                            else {
                                friendsViewHolder.vanish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            };
            friendsRecycler.setAdapter(firebaseRecyclerAdapter);
        }



    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView fullName;
        private TextView displayName;
        private TextView gender;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }


        void displayUser(User user) {
            fullName = itemView.findViewById(R.id.all_users_fullname);
            displayName = itemView.findViewById(R.id.all_users_display);
            gender = itemView.findViewById(R.id.all_users_gender);
            fullName.setText(user.full_name);
            displayName.setText(user.display_name);
            gender.setText(user.gender);
        }

        void vanish(){
            mView.findViewById(R.id.userViewHolderLayout).setVisibility(View.GONE);
            mView.findViewById(R.id.all_users_fullname).setVisibility(View.GONE);
            mView.findViewById(R.id.all_users_display).setVisibility(View.GONE);
            mView.findViewById(R.id.all_users_gender).setVisibility(View.GONE);

        }
    }
}
