package e.hp.mywhatsapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    DatabaseReference mFriendsDataase,mUsersDatabase;
    FirebaseAuth mAuth;

    String mCurrent_user_id;
    View mMainView;

    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter;



    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList=mMainView.findViewById(R.id.friends_list);
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFriendsDataase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDataase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query=mFriendsDataase.orderByValue();
        FirebaseRecyclerOptions firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query,Friends.class).build();

        friendsRecyclerViewAdapter =new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                final String list_user_id=getRef(position).getKey();
                assert list_user_id != null;
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String display_name=dataSnapshot.child("name").getValue().toString();
                        String user_thumb=dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(display_name);
                        holder.setUserImage(user_thumb,getContext());
                        if (dataSnapshot.hasChild("online")){
                            String userOnline=dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[]=new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click event for each option
                                        if (i==0){
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("userId",list_user_id);
                                            startActivity(profileIntent);
                                        }else {
                                            Intent  chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("userId",list_user_id);
                                            chatIntent.putExtra("userName",display_name);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.setDate(model.getDate());
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_single_layout,viewGroup,false);
                Log.e("creating view done","yes");
                return new FriendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        friendsRecyclerViewAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        friendsRecyclerViewAdapter.stopListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;


        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.e("vie hoder rus ","yes");
            mView=itemView;
        }
        void setDate(String date){
            TextView userNameView=mView.findViewById(R.id.users_single_status);
            userNameView.setText(date);
        }
        public  void setName(String name){
            TextView userNameView=mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);
        }
        void setUserImage(String thumb_image, Context ctx){
            CircleImageView mDisplayImage=mView.findViewById(R.id.users_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_image1).into(mDisplayImage);

            //we can also add offline feature picasso feature for fast retrieval

        }
        void setUserOnline(String online_icon){
            ImageView userOnlineView=mView.findViewById(R.id.user_single_online_icon);
            if (online_icon.equals("false")){
                userOnlineView.setVisibility(View.INVISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.VISIBLE);
            }
        }
    }

}
