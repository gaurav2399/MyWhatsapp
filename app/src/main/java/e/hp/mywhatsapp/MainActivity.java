package e.hp.mywhatsapp;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Toolbar mToolbar;
    ViewPager mViewPager;
    SectionPagerAdapter msectionPagerAdapter;
    TabLayout mTabLayout;
    DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("main activity","enter");

        mAuth = FirebaseAuth.getInstance();

        //tabs
        mViewPager=findViewById(R.id.main_tabPager);
        msectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(msectionPagerAdapter);
        mTabLayout=findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        if (mAuth.getCurrentUser()!=null)
        mUserReference= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //handling action bar
        mToolbar= findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Whatsapp");
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.e("in start","yes");
        if (currentUser==null){
            Log.e("perfect run","not done");
            sendToStart();
        }else {
            Log.e("perfect runn","done");
            //mUserReference.child("online").setValue("false");
        }
    }

    private void sendToStart() {
        Intent startIntent =new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
        }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null)
        mUserReference.child("online").setValue("false");
    }

    //handling menu icon

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.main_logout_btn:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_settings_btn:
                Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.main_all_btn:
                Intent usersIntent=new Intent(MainActivity.this,UsersActivity.class);
                startActivity(usersIntent);
                break;
        }
        return true;
    }


}
