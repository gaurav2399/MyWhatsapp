package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout mDisplayName,mEmail,mPassword;
    Button mCreateButton;
    androidx.appcompat.widget.Toolbar register_toolbar;
    ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.e("register activity","enter");

        //input fields

        mDisplayName=findViewById(R.id.reg_display_name);
        mEmail=(TextInputLayout)findViewById(R.id.reg_email);
        mPassword=findViewById(R.id.reg_password);
        mCreateButton=findViewById(R.id.reg_create_button);
        register_toolbar=findViewById(R.id.register_toolbar);
        mRegProgress=new ProgressDialog(this);

        //toolbar
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();


        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name=mDisplayName.getEditText().getText().toString().trim();
                String email=mEmail.getEditText().getText().toString().trim();
                String password=mPassword.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name,email,password);
                }


            }
        });
    }
    void register_user(final String display_name, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e("special friend","yes");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            String uid=user.getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String,String> userMap=new HashMap<>();
                            userMap.put("name",display_name);
                            userMap.put("status","Hi there,I'm using Gaurav Chat App.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token",deviceToken);
                            userMap.put("online","false");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mRegProgress.dismiss();
                                        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            FirebaseAuthException e=(FirebaseAuthException)task.getException();
                            assert e != null;
                            Log.e("error kya h:",e.getMessage());
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }

                        //also check email already exists or not error
                        //alos check internet connection problem

                        // ...
                    }
                });
    }
}
