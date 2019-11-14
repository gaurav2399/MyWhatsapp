package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //Layout Fields
    ImageView mProfileImage;
    TextView mProfileStatus,mProfileName,mProfileFriendCount;
    Button mProfileSendRequest,mProfileDeclineRequest;

    DatabaseReference mUsersDatabase,mFriendReqDatabase,mFriendsDatabase,mNotificationDatabase,mRootRef;
    static DatabaseReference mUserReference;

    FirebaseUser mCurrentUser;

    ProgressDialog mProgressDialog;

    String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //getting key of current user
        final String userId=getIntent().getStringExtra("userId");
        Log.e("user id value",userId);

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUsersDatabase.keepSynced(true);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendReqDatabase.keepSynced(true);
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsDatabase.keepSynced(true);
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mUserReference= FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        //allocating memory to fields
        mProfileName=findViewById(R.id.profile_display_name);
        mProfileImage=findViewById(R.id.display_profile);
        mProfileStatus=findViewById(R.id.profile_status);
        mProfileFriendCount=findViewById(R.id.totalFriends);
        mProfileSendRequest=findViewById(R.id.send_request_btn);
        mProfileDeclineRequest=findViewById(R.id.decline_req_btn);
        Log.e("all done","yes");

        current_state="not friends";

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("profile aciviry","function");

                String display_name=(String)dataSnapshot.child("name").getValue();
                String status=(String)dataSnapshot.child("status").getValue();
                Log.e("profile status",status);
                final String image=(String)dataSnapshot.child("image").getValue();
                String nowOnline = (String)dataSnapshot.child("online").getValue();
                Log.e("now status",nowOnline);

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_image1).into(mProfileImage);

                //------------------ FRIENDS LIST/REQUEST FEATURE -------------

                //complex part --> handling request status

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.e("in profile friend","function");

                        if (dataSnapshot.hasChild(userId)){
                            String req_type=dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                current_state="req_received";
                                mProfileSendRequest.setText("Accept Friend Request");
                                mProfileDeclineRequest.setVisibility(View.VISIBLE);
                                mProfileDeclineRequest.setEnabled(true);
                            }else if (req_type.equals("sent")){
                                current_state="req_sent";
                                mProfileSendRequest.setText("Cancel Friend Request");
                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }else {
                            mFriendsDatabase.child(mCurrentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.e("pta nhi kya chlgya","ha nhi to");

                                    if (dataSnapshot.hasChild(userId)){
                                        current_state="friends";
                                        mProfileSendRequest.setText("Unfriend this Person");

                                        mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRequest.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendRequest.setEnabled(false);

                // --------------- SENDING REQUEST -----------------

                if(current_state.equals("not friends")){
                    mProgressDialog.setMessage("Please wait while we sending the request");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.show();

                    DatabaseReference newNotificationref=mRootRef.child("notifications").child(userId).push();
                    String newNotificationId=newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap=new HashMap();
                    requestMap.put("Friend_request/"+mCurrentUser.getUid()+"/"+userId+"/request_type","sent");
                    requestMap.put("Friend_request/"+userId+"/"+mCurrentUser.getUid()+"/request_type","received");
                    requestMap.put("notifications/"+userId+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                         if (databaseError!=null){
                             Toast.makeText(ProfileActivity.this,"There was some error in sending request",Toast.LENGTH_SHORT).show();
                         }
                            current_state="req_sent";
                            mProfileSendRequest.setText("Cancel Friend Request");
                            mProfileSendRequest.setEnabled(true);
                            mProgressDialog.dismiss();

                        }
                    });
                }

                // - ------------ CANCEL REQUEST STATE -----------------

                if (current_state.equals("req_sent")){

                    mProgressDialog.setTitle("Cancelling Request");
                    mProgressDialog.setMessage("Please wait while we cancelling the request");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.show();

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequest.setEnabled(true);
                                    current_state="not friends";
                                    mProfileSendRequest.setText("Send Friend Request");
                                    mProfileSendRequest.setEnabled(true);

                                    mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRequest.setEnabled(false);

                                    mProgressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this,"Cancelling Request Failed!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            mProgressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this,"Cancelling Request Failed!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //----------- ACCEPTING REQUEST -----------------

                if (current_state.equals("req_received")){
                    mProgressDialog.setTitle("Request Accepting..");
                    mProgressDialog.setMessage("Please wait while we accepting the request");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.show();

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap=new HashMap();
                    friendMap.put("Friends/"+mCurrentUser.getUid()+"/"+userId+"/date",currentDate);
                    friendMap.put("Friends/"+userId+"/"+mCurrentUser.getUid()+"/date",currentDate);

                    friendMap.put("Friend_request/"+mCurrentUser.getUid()+"/"+userId,null);
                    friendMap.put("Friend_request/"+userId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError==null){
                                mProfileSendRequest.setEnabled(true);
                                current_state="friends";
                                mProfileSendRequest.setText("Unfriend this person");

                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                                mProgressDialog.dismiss();
                            }else {
                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //--------------- UNFRIEND THE PERSON ---------------
                if (current_state.equals("friends")){
                    Toast.makeText(ProfileActivity.this,"This process under developing.",Toast.LENGTH_SHORT);
                    Map unFriendMap=new HashMap();
                    unFriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+userId,null);
                    unFriendMap.put("Friends/"+userId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError==null){

                                current_state="not_friends";
                                mProfileSendRequest.setText("Send friend request");

                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                            }else {
                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendRequest.setEnabled(true);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser!=null){
            mUserReference.child("online").setValue("false");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCurrentUser!=null)
        mUserReference.child("online").setValue("false");
    }
}
