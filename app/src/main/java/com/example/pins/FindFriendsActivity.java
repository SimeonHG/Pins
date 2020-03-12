package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.actions.SearchIntents;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {


    private Button searchBtn;
    private EditText searchInputText;

    private RecyclerView friendsList;

    public DatabaseReference  allUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        friendsList = findViewById(R.id.recycler);
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(this));


        searchInputText = findViewById(R.id.search_input);


        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        searchBtn = findViewById(R.id.search_button);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = searchInputText.getText().toString();
                searchPeople(input);
            }
        });
    }

    private void searchPeople(String input) {
        Query searchPeopleQuery = allUsersRef.orderByChild("full_name").startAt(input).endAt(input + "\uf8ff");

        FirebaseRecyclerAdapter<User, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FindFriendsViewHolder>
                (
                    User.class, R.layout.all_users_display_layout, FindFriendsViewHolder.class, searchPeopleQuery
                ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, User user, final int position) {
                viewHolder.displayUser(user);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        friendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView fullName;
        private TextView displayName;
        private TextView gender;
        private ImageView profilePic;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            fullName = itemView.findViewById(R.id.all_users_fullname);
            displayName = itemView.findViewById(R.id.all_users_display);
            gender = itemView.findViewById(R.id.all_users_gender);
            profilePic = itemView.findViewById(R.id.all_users_pic);
        }


        void displayUser(User user) {
            fullName.setText(user.full_name);
            displayName.setText(user.display_name);
            gender.setText(user.gender);
            Picasso.get().load(user.profilePic).placeholder(R.drawable.genericpic).into(profilePic);

        }
    }
}
