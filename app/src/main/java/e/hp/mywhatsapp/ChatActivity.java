package e.hp.mywhatsapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    TextView mTitleView,mLastSeenView;
    CircleImageView mProfileImage;
    ImageView mChatAddBtn,mChatSendBtn;
    EditText mMessageView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseAuth mAuth;
    String mCurrentUserId;
    List<Messages> messagesList=new ArrayList<>();
    LinearLayoutManager mLinearLayout;
    RecyclerView mMessagesList;
    MessageAdapter mAdaptor;
    static  int TOTAL_ITEMS_TO_LOAD=10;
    int mCurrentPage=1,itemPos=0;
    String mLastKey="";
    private String mPrefKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("chat activity","runs");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatUser=getIntent().getStringExtra("userId");
        mChatToolbar=findViewById(R.id.chat_app_bar);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mChatAddBtn=findViewById(R.id.add_message);
        mChatSendBtn=findViewById(R.id.send_message);
        mMessageView=findViewById(R.id.text_message);

        mMessagesList=findViewById(R.id.messages_list);
        mSwipeRefreshLayout=findViewById(R.id.swipe_refresh);
        mLinearLayout=new LinearLayoutManager(this);
        mAdaptor=new MessageAdapter(messagesList);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdaptor);

        setSupportActionBar(mChatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        mRootRef= FirebaseDatabase.getInstance().getReference();
        final String chatUserName=getIntent().getStringExtra("userName");
        getSupportActionBar().setTitle(chatUserName);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        mTitleView=findViewById(R.id.display_name);
        mLastSeenView=findViewById(R.id.user_last_seen);
        mProfileImage=findViewById(R.id.custom_bar_image);



        mTitleView.setText(chatUserName);
        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("online")) {
                    String online = dataSnapshot.child("online").getValue().toString();

                    if (online.equals("true")) {
                        mLastSeenView.setText("Online");
                    } else {
                        //GetTimeAgo getTimeAgo=new GetTimeAgo();
                        //long lastSeen=Long.parseLong(online);
                        //String lastSeenTime=getTimeAgo.getTimeAgo(lastSeen,getApplicationContext());
                        mLastSeenView.setText("Offline");
                    }
                }
                    String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_image1).into(mProfileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mChatUser)){
                    Log.e("unwanted running","done");
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen","false");
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId,chatAddMap);



                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Log.d("Chat LOg",databaseError.getMessage());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });

    }

    private void loadMoreMessages() {
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                String messageKey=dataSnapshot.getKey();
                Log.e("Message is : ",message.toString());
                if (!mPrefKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }else {
                    mPrefKey=mLastKey;
                }

                if (itemPos==1){
                    mLastKey=messageKey;
                }
                Log.d("TOtal keys","Last key"+mLastKey+"| Prev key :"+mPrefKey+" |Message key : "+messageKey);
                mAdaptor.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                mSwipeRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message=mMessageView.getText().toString();
        String current_user_ref="messages/"+mCurrentUserId+"/"+mChatUser+"/";
        String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUserId+"/";

        DatabaseReference user_reference_push=mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push();
        String push_id=user_reference_push.getKey();
        if (!TextUtils.isEmpty(message)){
            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen","false");
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

            mMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError!=null){
                        Log.d("Error in  messages",databaseError.getMessage());
                    }else{
                        Toast.makeText(ChatActivity.this, "Message send", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void loadMessages(){
        Log.e("loadMessage is called","yes");
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Toast.makeText(ChatActivity.this,"child is added",Toast.LENGTH_SHORT).show();
                Messages message=dataSnapshot.getValue(Messages.class);
                Log.e("Message is : ",message.toString());
                itemPos++;

                if (itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    Log.e("last mesage key",messageKey);
                    mLastKey=messageKey;
                    mPrefKey=messageKey;
                }

                messagesList.add(message);
                mAdaptor.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
