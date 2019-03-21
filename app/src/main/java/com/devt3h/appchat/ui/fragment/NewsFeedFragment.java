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
import com.devt3h.appchat.adapter.NewsfeedAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.Friend;
import com.devt3h.appchat.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedFragment extends Fragment{
    private RecyclerView recyclerView;
    private List<Post> modelFeeds;
    private List<String> listIdFriend;
    private NewsfeedAdapter adapter;
    private String idCurrentId = FirebaseAuth.getInstance().getUid();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Constants.ARG_FRIENDS);
    private DatabaseReference pDatabase = FirebaseDatabase.getInstance().getReference(Constants.ARG_POST);
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
        recyclerView = view.findViewById(R.id.rvfeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        modelFeeds = new ArrayList<>();
        listIdFriend = new ArrayList<>();

        getIdFriend();
        showPost(listIdFriend);
        return view;
    }

    private void showPost(List<String> listIdFriend) {
        pDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if(dataSnapshot.exists()){
                    if(post.getUid().equals(idCurrentId)){
                        modelFeeds.add(post);
                    }else{
                        for(int i=0; i< listIdFriend.size(); i++){
                            if(post.getUid().equals(listIdFriend.get(i))){
                                modelFeeds.add(post);
                                listIdFriend.remove(i);
                            }
                        }
                    }
                }
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }else {
                    adapter = new NewsfeedAdapter(new NewsfeedAdapter.IPost() {
                        @Override
                        public int getCount() {
                            if(modelFeeds==null) return 0;
                            return modelFeeds.size();
                        }

                        @Override
                        public Post getPost(int position) {
                            return modelFeeds.get(position);
                        }
                    });
                }
                recyclerView.setAdapter(adapter);
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

    public void getIdFriend() {

        mDatabase.child(Constants.ARG_FRIENDS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                if(dataSnapshot.exists()){
                    if (friend.getSender_id().equals(idCurrentId) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
                        listIdFriend.add(friend.getReceiver_id());

                    } else if (friend.getReceiver_id().equals(idCurrentId) && friend.getStatus().equals(Constants.KEY_ACCEPTED)) {
                        listIdFriend.add(friend.getSender_id());
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

    }
}
