package e.hp.mywhatsapp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class GauravChat extends Application {

    private DatabaseReference mUsersDatabse;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /* Picasso offline feature developing */
        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

//        mAuth=FirebaseAuth.getInstance();
//        if (mAuth.getCurrentUser()!=null) {
//            mUsersDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
//            mUsersDatabse.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    Log.e("gaurav chat","ha g");
//                    if (dataSnapshot != null) {
//                        Log.e("online","false");
//                       // mUsersDatabse.child("online").onDisconnect().setValue("false");
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
    }
}
