package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    Toolbar mToolBar;
    TextInputLayout mStatus;
    Button mSavebtn;

    //Firebase
    DatabaseReference mStatusDatabase;
    FirebaseUser mCurrentUser;

    //Progress
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Log.e("status activity","enter");

        // setting variables
        mToolBar=findViewById(R.id.status_appBar);
        mSavebtn=findViewById(R.id.status_save_btn);
        mStatus=findViewById(R.id.status_input);

        // getting current user
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=mCurrentUser.getUid();
        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        // toolbar
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value=getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                mProgress=new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();
                String status=mStatus.getEditText().getText().toString().trim();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            finish();
                            mProgress.dismiss();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
