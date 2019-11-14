package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    Toolbar mUsersToolbar;
    RecyclerView mUsersList;

    FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("eter into on craete","yes");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Toolbar
        mUsersToolbar=findViewById(R.id.all_user_toolbar);
        setSupportActionBar(mUsersToolbar);
        if(getSupportActionBar()!=null) {
            Log.e("toolbar done","yes");
            getSupportActionBar().setTitle("All Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // linking to database
        Log.e("link to db","reach");
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        Log.e("out of db","no");
        mUsersList=findViewById(R.id.users_list);
        mUserDatabase.keepSynced(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        // Progress dialog
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading All Users");
        mProgressDialog.setMessage("Please wait while we loading all users.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        // Setting query
        Query query= mUserDatabase.orderByValue();

        // setting recycler view
        FirebaseRecyclerOptions firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Users>().setQuery(query,Users.class).build();
        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                Log.e("model we get:::",model.toString());
                if(model.getStatus()==null)
                    Log.e("status got null","true");
                else {
                    Log.e("status ::", model.getStatus());
                    holder.userName.setText(model.getName());
                    holder.userStatus.setText(model.getStatus());
                    holder.setUserImage(model.getThumb_image(),getApplicationContext());
                    if(model.getOnline().equals("false"))
                        holder.mUserOnline.setVisibility(View.INVISIBLE);
                }
//                holder.setUserImage(model.getThumb_image(),getApplicationContext());
//                if(model.getOnline().equals("false"))
//                    holder.mUserOnline.setVisibility(View.INVISIBLE);
                mProgressDialog.dismiss();

                final String userId=getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("userId",userId);
                        startActivity(profileIntent);
                    }
                });
                //holder.setImage(model.getImage());
                Log.e("binding done","yes");
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_single_layout,viewGroup,false);
                Log.e("creating view done","yes");
                return new UsersViewHolder(view);
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView userName,userStatus;
        CircleImageView mDisplayImage;
        ImageView mUserOnline;
        UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.e("view holder","done");
            mView=itemView;
            userName=itemView.findViewById(R.id.users_single_name);
            userStatus=itemView.findViewById(R.id.users_single_status);
            mDisplayImage=itemView.findViewById(R.id.users_single_image);
            mUserOnline=itemView.findViewById(R.id.user_single_online_icon);
        }
        void setUserImage(String thumb_image, Context ctx){
            Log.e("settting image dpone","yes");
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_image1).into(mDisplayImage);
        }

    }
}
