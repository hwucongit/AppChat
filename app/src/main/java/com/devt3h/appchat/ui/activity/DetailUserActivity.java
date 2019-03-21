package com.devt3h.appchat.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.Friend;
import com.devt3h.appchat.model.User;
import com.devt3h.appchat.ui.fragment.AcceptDeclineFragment;
import com.devt3h.appchat.ui.fragment.SendMessageFragment;
import com.devt3h.appchat.ui.fragment.SendRequestFragment;
import com.devt3h.appchat.ui.fragment.UnfriendFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailUserActivity extends AppCompatActivity {
    private TextView tvUsername, tvBirthday;
    private ImageView imgAvatar, imgCover;

    private DatabaseReference databaseReference;

    private String CURRENT_SATE;
    private String key;
    private String userCurrentId;

    private FragmentManager manager;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_user);

        user_id = getIntent().getExtras().get(Constants.KEY_VISIT_USER_ID).toString();

        inits();


            databaseReference.child(Constants.ARG_USERS).child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    AccountUser user = dataSnapshot.getValue(AccountUser.class);
                    if(user!=null){
                        tvUsername.setText(user.getName());
                        tvBirthday.setText(user.getBirthday());
                        if(!user.getAvatarURL().equals(Constants.KEY_DEFAULT)){
                            Picasso.get().load(user.getAvatarURL())
                                    .resize(getResources().getDimensionPixelSize(R.dimen.size_image),
                                            getResources().getDimensionPixelSize(R.dimen.size_image))
                                    .centerCrop()
                                    .into(imgAvatar);
                        }

                        if(!user.getCoverURL().equals(Constants.KEY_DEFAULT)){
                            Picasso.get().load(user.getCoverURL())
                                    .resize(250, 250)
                                    .centerCrop()
                                    .into(imgCover);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Friend friend = dataSnapshot.getValue(Friend.class);

                    if(friend.getReceiver_id().equals(userCurrentId) && friend.getSender_id().equals(user_id)){
                        if(friend.getStatus().equals(Constants.ACCEPTED)){
                            CURRENT_SATE = Constants.ACCEPTED;
                        }else {
                            CURRENT_SATE = Constants.YOU_RECEIVE_REQUEST;
                        }
                        key = dataSnapshot.getKey();
                    }else if(friend.getReceiver_id().equals(user_id) && friend.getSender_id().equals(userCurrentId)){
                        if(friend.getStatus().equals(Constants.ACCEPTED)){
                            CURRENT_SATE = Constants.ACCEPTED;
                        }else {
                            CURRENT_SATE = Constants.YOU_REQUEST_FRIEND;
                        }
                        key = dataSnapshot.getKey();
                    }

                    if(CURRENT_SATE==null) CURRENT_SATE = Constants.NOT_FRIEND;

                    getFragment(CURRENT_SATE, key);
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
            SendMessageFragment sendMessageFragment = new SendMessageFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString("user_id",user_id);
            sendMessageFragment.setArguments(bundle);
            transaction.add(R.id.fl_send_message,sendMessageFragment,SendMessageFragment.class.getName());
            transaction.commit();
        }

    private void inits() {
        tvUsername = findViewById(R.id.tv_username);
        tvBirthday = findViewById(R.id.tv_birthday);
        imgAvatar = findViewById(R.id.img_avatar);
        imgCover = findViewById(R.id.img_cover);

        manager = getSupportFragmentManager();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userCurrentId = FirebaseAuth.getInstance().getCurrentUser().getUid();



    }

    public void getFragment(String currentState, String key) {
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        if(key != null){
            bundle.putString("key", key);
        }else {
            bundle.putString("user_id", user_id);
        }

        switch (currentState){
            case Constants.ACCEPTED:
                UnfriendFragment unfriendFragment = new UnfriendFragment();
                unfriendFragment.setArguments(bundle);
                //transaction.add(R.id.fl_content_button, unfriendFragment, UnfriendFragment.class.getName());
                transaction.replace(R.id.fl_content_button, unfriendFragment, UnfriendFragment.class.getName());
                transaction.commit();
                break;
            case Constants.YOU_RECEIVE_REQUEST:
                AcceptDeclineFragment acceptDeclineFragment = new AcceptDeclineFragment();
                acceptDeclineFragment.setArguments(bundle);
                transaction.replace(R.id.fl_content_button, acceptDeclineFragment, AcceptDeclineFragment.class.getName());
                transaction.commit();
                break;
            case Constants.YOU_REQUEST_FRIEND:
                Helper.showToast(this, getResources().getString(R.string.requested_friend));
                break;
            case Constants.NOT_FRIEND:
                SendRequestFragment sendRequestFragment = new SendRequestFragment();
                sendRequestFragment.setArguments(bundle);
                transaction.add(R.id.fl_content_button, sendRequestFragment, SendRequestFragment.class.getName());
                transaction.commit();
                break;
            default:
                break;
        }
    }
}
