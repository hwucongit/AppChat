package com.devt3h.appchat.ui.fragment;

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
import com.devt3h.appchat.adapter.UserAdapter;
import com.devt3h.appchat.com.MyApplication;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.Friend;
import com.devt3h.appchat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FriendsFragment extends Fragment implements UserAdapter.IUser {
    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<AccountUser> friendList;
    private List<String> listId;
    private Disposable disposableStatusFriend;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        rvUsers = v.findViewById(R.id.rv_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(container.getContext()));
        friendList = new ArrayList<>();
        listId = new ArrayList<>();

        readUser();
        return v;
    }

    private void readUser() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // read data form table Friend
        databaseReference.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                String userId = user.getUid();
                if (user != null) {
                    if (friend.getSender_id().equals(user.getUid()) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
                        listId.add(friend.getReceiver_id());

                    } else if (friend.getReceiver_id().equals(user.getUid()) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
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

        // read data from table Users with userId of table Friend
        databaseReference.child(Constants.ARG_USERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateAdapterFrient(dataSnapshot);
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

    private void checkUpdateListFriend(DataSnapshot dataSnapshot) {
        if (friendList == null || friendList.size() == 0) {
            return;
        }
        if (dataSnapshot == null) {
            return;
        }
        List<User> userStatuss = new ArrayList<>();
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
            User user = postSnapshot.getValue(User.class);
            if (user == null){
                continue;
            }
            for ( int i = 0; i < friendList.size(); i++){
                if ( friendList.get(i).getId().equals(user.getId())){
                    userStatuss.add(user);
                    break;
                }
            }
        }

        if(userStatuss.size() > 0){
            boolean isNo = false;
            for ( int i = 0; i < friendList.size(); i++){
                boolean isNoChild = false;
                for ( int j = 0;  j < userStatuss.size(); j++){
                    if(friendList.get(i).getId().equals(userStatuss.get(j).getId())){
                        if(friendList.get(i).isOnline() != userStatuss.get(j).isOnline()){
                            friendList.get(i).setOnline(true);
                            isNo = true;
                            isNoChild=true;
                        }
                        break;
                    }
                }
                if(isNoChild){
                    friendList.get(i).setOnline(false);
                }
            }
            if(isNo){
                Observable.just(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(re->{
                            adapter.notifyDataSetChanged();
                        });
            }
        }


    }

    private void updateAdapterFrient(DataSnapshot dataSnapshot) {
        AccountUser user = dataSnapshot.getValue(AccountUser.class);
        if (listId != null) {
            for (int i = 0; i < listId.size(); i++) {
                if (user.getId().equals(listId.get(i))) {
                    friendList.add(user);
                    listId.remove(i);
                }
            }
        }
        if (adapter == null) {
            adapter = new UserAdapter(this, true);
            rvUsers.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

    }


    @Override
    public int getCount() {
        if (friendList == null) return 0;
        return friendList.size();
    }

    @Override
    public AccountUser getUser(int position) {
        return friendList.get(position);
    }

    private void getStatusFriend() {
        if (disposableStatusFriend != null && !disposableStatusFriend.isDisposed()) {
            Observable.create((ObservableOnSubscribe<Boolean>) t -> {
                long currentTime = new Date().getTime() - MyApplication.getOffsetFromUtc();
                databaseReference.child(Constants.ARG_USERS).orderByChild("lastUpdateStatus").endAt(currentTime).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        checkUpdateListFriend(dataSnapshot);
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
                t.onNext(true);
                t.onComplete();
            })

                    .retryWhen(error -> error.delay(6, TimeUnit.SECONDS))
                    .repeatWhen(complete -> complete.delay(6, TimeUnit.SECONDS))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {

                    });
        }
    }

    private void disConnectStatusFrient(){
        if(disposableStatusFriend!= null && !disposableStatusFriend.isDisposed()){
            disposableStatusFriend.dispose();
            disposableStatusFriend = null;
        }
    }

}
