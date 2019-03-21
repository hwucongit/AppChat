package com.devt3h.appchat.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.Friend;
import com.devt3h.appchat.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UnfriendFragment extends Fragment implements View.OnClickListener {
    private Button btnUnfriend;
    private List<String> listId;
    private List<String> listPost;
    private TextView tvPost, tvFollowers;
    private String key, friendId;
    private String userId = FirebaseAuth.getInstance().getUid();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_unfriend, container, false);
        inits(v);
        mDatabase.child(Constants.ARG_FRIENDS).child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if(friend.getReceiver_id().equals(userId)){
                    friendId = friend.getSender_id();
                }else{
                    friendId = friend.getReceiver_id();
                }
                showInforFriend(friendId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return v;
    }

    private void showInforFriend(String friendId) {
       mDatabase.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               Friend friend = dataSnapshot.getValue(Friend.class);
               if(friend!=null){
                   if (friend.getSender_id().equals(friendId) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
                       listId.add(friend.getReceiver_id());
                   } else if (friend.getReceiver_id().equals(friendId) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
                       listId.add(friend.getReceiver_id());
                   }

                   int count = listId.size();
                   tvFollowers.setText(count + " \nFollowers");
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
        mDatabase.child(Constants.ARG_POST).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if(post!=null){
                    if(post.getUid().equals(friendId)){
                        listPost.add(post.getUid());
                    }

                    int count = listPost.size();
                    tvPost.setText(count + " \nPosts");
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

    private void inits(View v) {
        btnUnfriend = v.findViewById(R.id.btn_unfriend);
        tvPost = v.findViewById(R.id.tv_post);
        tvFollowers = v.findViewById(R.id.tv_followers);

        key = getArguments().getString("key");
        listId = new ArrayList<>();
        listPost = new ArrayList<>();

        btnUnfriend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        unfriend(key);
        getActivity().finish();
    }

    private void unfriend(String key) {
        Task task = mDatabase.child(key).removeValue();
        if(task.isSuccessful()){
            Helper.showToast(getContext(), getResources().getString(R.string.unfriend_done));
        }else {
            Helper.showToast(getContext(), getResources().getString(R.string.setting_error));
        }
    }
}
