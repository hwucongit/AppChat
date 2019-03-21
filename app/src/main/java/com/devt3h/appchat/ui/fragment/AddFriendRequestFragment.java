package com.devt3h.appchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devt3h.appchat.R;
import com.devt3h.appchat.adapter.RequestFriendAdapter;
import com.devt3h.appchat.adapter.UserAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.Friend;
import com.devt3h.appchat.model.User;
import com.devt3h.appchat.ui.activity.DetailUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddFriendRequestFragment extends Fragment {
    public static final int DETAIL_USER = 1;
    private RequestFriendAdapter userAdapter;
    private List<User> requestFriendList;
    private List<String> listId;
    private String key;
    private String userId;
    private RecyclerView rvRequestFriend;
    private DatabaseReference databaseReference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request_friend, container, false);
        rvRequestFriend = v.findViewById(R.id.rv_request_friend);
        rvRequestFriend.setLayoutManager(new LinearLayoutManager(container.getContext()));

        requestFriendList = new ArrayList<>();
        listId = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        changeIsSee();

        readRequestFriend();
        return v;
    }

    private void readRequestFriend() {

        databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if(friend!=null && friend.getStatus().equals(Constants.KEY_PENDING)){
                    if(friend.getReceiver_id().equals(userId)){
                        listId.add(friend.getSender_id());
                    }
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

        databaseReference.child(Constants.ARG_USERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);

                if(listId!=null){
                    for(int i=0; i<listId.size(); i++){
                        if(user.getId().equals(listId.get(i))){
                            requestFriendList.add(user);
                            listId.remove(i);
                        }
                    }
                }

                userAdapter = new RequestFriendAdapter(new RequestFriendAdapter.IRequestFriend() {
                    @Override
                    public int getCount() {
                        if(requestFriendList==null) return 0;
                        return requestFriendList.size();
                    }

                    @Override
                    public User getFriend(int position) {
                        return requestFriendList.get(position);
                    }

                    @Override
                    public void acceptRequestFriend(int postion) {
                        acceptRequest(postion);
                        Helper.showToast(getContext(), getResources().getString(R.string.accept_successeful));
                    }

                    @Override
                    public void declineRequestFriend(int position) {
                        declineRequest(position);
                        Helper.showToast(getContext(), getResources().getString(R.string.decline_done));
                    }

                    @Override
                    public void showDetailUser(int position) {
                        String user_id = requestFriendList.get(position).getId();
                        Intent iDetailUser = new Intent(getActivity(), DetailUserActivity.class);
                        iDetailUser.putExtra(Constants.KEY_VISIT_USER_ID, user_id );
                        startActivityForResult(iDetailUser, DETAIL_USER);
                    }
                });

                rvRequestFriend.setAdapter(userAdapter);
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

    private void acceptRequest(final int position){
        final User user = requestFriendList.get(position);
        databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sender_id = dataSnapshot.child("sender_id").getValue().toString();

                if(user.getId().equals(sender_id)){
                    databaseReference.child(Constants.ARG_FRIENDS).child(dataSnapshot.getKey()).child(Constants.KEY_STATUS)
                            .setValue(Constants.KEY_ACCEPTED);
                    requestFriendList.remove(position);
                    userAdapter.notifyDataSetChanged();
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

    private void declineRequest(final int position){
        final User user = requestFriendList.get(position);
        databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sender_id = dataSnapshot.child("sender_id").getValue().toString();

                if(user.getId().equals(sender_id)){
                    databaseReference.child(Constants.ARG_FRIENDS).child(dataSnapshot.getKey()).removeValue();
                    requestFriendList.remove(position);
                    userAdapter.notifyDataSetChanged();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==DETAIL_USER){
            userAdapter.notifyDataSetChanged();
            requestFriendList.clear();
            readRequestFriend();
        }
    }

    private void changeIsSee(){
        databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if(friend.getReceiver_id().equals(userId)){
                    databaseReference.child(Constants.ARG_FRIENDS).child(dataSnapshot.getKey()).child("is_see").setValue("seen");
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
}
