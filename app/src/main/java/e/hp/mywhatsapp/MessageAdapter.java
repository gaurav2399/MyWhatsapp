package e.hp.mywhatsapp;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    List<Messages> mMessageList;
    FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> messagesList) {
        Log.e("Message adaptorm phucha","yes");
        this.mMessageList=messagesList;
        mAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Log.e("view create to ho rha h","yes");
        View v= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single,viewGroup,false);
        return new MessageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder messageHolder, int i) {

        String current_user_id=mAuth.getCurrentUser().getUid();
        Log.e("current userId",current_user_id);
        Log.e("view bind to ho rha h","yes");
        Messages c=mMessageList.get(i);
        String from_user=c.getFrom();
        //Log.e("from userId",from_user);
        if ((from_user!=null)&&(from_user.equals(current_user_id))){

            messageHolder.messageText.setBackgroundResource(R.drawable.sender_message_background);
            messageHolder.messageText.setTextColor(Color.BLACK);
            messageHolder.messageText.layout(0,5,10,0);
            messageHolder.mProfileImage.setVisibility(View.INVISIBLE);
        }else {
            messageHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageHolder.messageText.setTextColor(Color.WHITE);
            messageHolder.messageText.layout(55,5,0,0);
            messageHolder.mProfileImage.setVisibility(View.VISIBLE);
        }
        messageHolder.messageText.setText(c.getMessage());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder{
        TextView messageText;
        CircleImageView mProfileImage;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);


            messageText=itemView.findViewById(R.id.message_text_layout);
            mProfileImage=itemView.findViewById(R.id.message_profile_layout);
        }

    }

}
