package e.hp.mywhatsapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String CHANNEL_ID="Gaurav_App";
    private static  final String CHANNEL_NAME="Chat_App";
    private static  final String CHANNEL_DESCRIPTION="Chatting" ;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("gdbad h","yes");
        super.onMessageReceived(remoteMessage);
        Log.e("message to aya h","yes");

        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager mNotifyMgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.createNotificationChannel(channel);
        }
        displayNotification();*/
        String notificationTitle=remoteMessage.getNotification().getTitle();
        String notificationMessage=remoteMessage.getNotification().getBody();

        String clickAction=remoteMessage.getNotification().getClickAction();
        Log.e("clickAction m kya h",clickAction);
        String from_user_id=remoteMessage.getData().get("From_id");
        Log.e("reach to that id_page",from_user_id);
        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this)
                                    .setContentTitle(notificationTitle)
                                    .setContentText(notificationMessage)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.new_friend_request2);
            Log.e("Lollipop se >= ","android version h");
            // notification.setColor(getResources().getColor(R.color.notification_color));
        } else {
            mBuilder.setSmallIcon(R.drawable.my_notification);
            Log.e("Lollipop se <= ","android version h");
        }

        Intent resultIntent=new Intent(clickAction);
        resultIntent.putExtra("userId",from_user_id);

        PendingIntent resultPendingIntent=PendingIntent
                .getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId=(int)System.currentTimeMillis();
        NotificationManager mNotifyMgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId,mBuilder.build());

    }

    private void displayNotification(){
        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(this,CHANNEL_ID)
                        .setContentTitle("Friend Request")
                        .setContentText("You've received a new friend Request")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.my_notification);
           // notification.setColor(getResources().getColor(R.color.notification_color));
        } else {
            mBuilder.setSmallIcon(R.drawable.my_notification);
        }

        NotificationManagerCompat mNotiCompMgr=NotificationManagerCompat.from(this);
        int mNotificationId=(int)System.currentTimeMillis();
        mNotiCompMgr.notify(mNotificationId,mBuilder.build());
    }
}
