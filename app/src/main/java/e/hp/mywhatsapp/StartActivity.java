package e.hp.mywhatsapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    Button start_reg_button,start_login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.e("start activity","enter");
        start_reg_button=findViewById(R.id.start_reg_button);
        start_login_btn=findViewById(R.id.start_login_btn);

        //starting login activity

        start_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login_intent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(login_intent);
            }
        });

        //starting register activity

        start_reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });
    }
}
