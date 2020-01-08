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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.actions.SearchIntents;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FindFriendsActivity extends AppCompatActivity {


    private Button searchBtn;
    private EditText searchInputText;

    private RecyclerView Results;

    private DatabaseReference  allUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        Results = (RecyclerView) findViewById(R.id.recycler);
        Results.setHasFixedSize(true);
        Results.setLayoutManager(new LinearLayoutManager(this));

        searchBtn = findViewById(R.id.search_button);
        searchInputText = findViewById(R.id.search_input);


        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = searchInputText.getText().toString();
                SearchPeople(input);
            }
        });
    }

    private void SearchPeople(String input) {
        Query searchPeopleQuery = allUsersRef.orderByChild("full_name").startAt(input).endAt(input + "\uf8ff");

        FirebaseRecyclerAdapter<User,FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, FindFriendsViewHolder>
                (
                    User.class, R.layout.all_users_display_layout, FindFriendsViewHolder.class, searchPeopleQuery
                ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, User user, final int position) {
                viewHolder.setFullname(user.full_name);
                viewHolder.setDisplayname(user.display_name);
                Toast.makeText(FindFriendsActivity.this, "Username: "+ user.display_name,  Toast.LENGTH_SHORT).show();
                viewHolder.setGender(user.gender);

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
        Results.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname){
            TextView fullName = (TextView) mView.findViewById(R.id.all_users_fullname);
            fullName.setText(fullname);
        }
        public void setDisplayname(String displayname){
            TextView displayName = (TextView) mView.findViewById(R.id.all_users_display);
            displayName.setText(displayname);
        }
        public void setGender(String gender){
            TextView gen = (TextView) mView.findViewById(R.id.all_users_gender);
            gen.setText(gender);
        }


    }
}
