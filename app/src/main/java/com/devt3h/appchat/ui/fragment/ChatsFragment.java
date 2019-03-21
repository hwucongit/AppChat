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
import com.devt3h.appchat.adapter.HistoryChatAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.Chat;
import com.devt3h.appchat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView rvHistoryChat;
    private HistoryChatAdapter historyChatAdapter;
    private List<User> mUsers;
    private List<String> listUserChat;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_chat, container, false);
        inits(view);
        return view;
    }
    private void inits(View view){
        rvHistoryChat = view.findViewById(R.id.rv_history_chat);

        rvHistoryChat.setHasFixedSize(true);
        LinearLayoutManager historyChatLayout = new LinearLayoutManager(getContext());
        rvHistoryChat.setLayoutManager(historyChatLayout);

        mUsers = new ArrayList<>();
        listUserChat = new ArrayList<>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUserChat.clear();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Chat chat = data.getValue(Chat.class);
                    if(currentUser.getUid().equals(chat.getSender_id())){
                        listUserChat.add(chat.getReceiver_id());
                    }
                    if(currentUser.getUid().equals(chat.getReceiver_id())){
                        listUserChat.add(chat.getSender_id());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readChats() {
        reference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    for (String userChatId: listUserChat) {
                        if(user.getId().equals(userChatId)){
                            if(!exitsUser(user.getId())) {
                                mUsers.add(user);
                            }
                        }

                    }
                }
                historyChatAdapter = new HistoryChatAdapter(mUsers);
                rvHistoryChat.setAdapter(historyChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private boolean exitsUser(String userId){
        for (User user: mUsers){
            if(user.getId().equals(userId))
                return true;
        }
        return false;
    }
}
