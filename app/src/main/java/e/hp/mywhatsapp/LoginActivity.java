package e.hp.mywhatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout mLoginEmail;
    TextInputLayout mLoginPassword;
    Toolbar loginToolbar;
    Button mLogin;
    ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.e("login activity","enter");

        //input fields
        loginToolbar=findViewById(R.id.login_toolbar);
        mLoginEmail=findViewById(R.id.login_email);
        mLoginPassword=findViewById(R.id.login_password);
        mLogin=findViewById(R.id.login_button);

        //actionbar
        setSupportActionBar(loginToolbar);
        mLoginProgress=new ProgressDialog(this);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=mLoginEmail.getEditText().getText().toString().trim();
                String password=mLoginPassword.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    mLoginProgress.setTitle("Logging in");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                  loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    mLoginProgress.dismiss();
                    String user_id=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(user_id).child("device_token")
                            .setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }
                    });

                }
                else {
                    mLoginProgress.hide();
                    FirebaseAuthException e=(FirebaseAuthException)task.getException();
                    Log.e("error kya h:",e.getMessage());
                    Toast.makeText(getApplicationContext(),"Cannot Sign in. Please check the form and try again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
