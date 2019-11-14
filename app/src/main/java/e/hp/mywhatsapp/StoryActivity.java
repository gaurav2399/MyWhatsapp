package e.hp.mywhatsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class StoryActivity extends AppCompatActivity {
    ImageView addImage,addText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        addImage=findViewById(R.id.addPhoto);
        addText=findViewById(R.id.addText);
    }
}
