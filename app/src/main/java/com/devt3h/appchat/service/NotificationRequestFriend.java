package com.devt3h.appchat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationRequestFriend extends Service {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private BroadCastService broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("CLOSE");
        broadcastReceiver = new BroadCastService();
        registerReceiver(broadcastReceiver, filter);
    }

    public void showNotification() {
        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_FRIENDS);
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if(friend.getReceiver_id().equals(userID) && friend.getIs_see().equals(Constants.KEY_DEFAULT)){
                    openNotification();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openNotification() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel chal = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chal);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle("Chat App");
        builder.setContentText(getResources().getString(R.string.notification_add_friend));
        builder.setSmallIcon(R.drawable.ic_person_add);

        Intent intent = new Intent();
        intent.setAction("CLOSE");

        PendingIntent pending =
                PendingIntent.getBroadcast(this, 100,
                        intent, 0);
        builder.setContentIntent(
                pending
        );


        startForeground(10, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder(NotificationRequestFriend.this);
    }

    public static class MyBinder extends Binder {
        private NotificationRequestFriend notificationRequestFriend;

        public MyBinder(NotificationRequestFriend notification){
            this.notificationRequestFriend = notification;
        }
        public NotificationRequestFriend getNotificationRequestFriend(){
            return notificationRequestFriend;
        }
    }

    private class BroadCastService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            NotificationManager mrg =
                    (NotificationManager)getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );
            mrg.cancel(10);
        }
    }

}
