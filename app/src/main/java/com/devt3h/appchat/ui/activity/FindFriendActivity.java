package com.devt3h.appchat.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Space;

import com.devt3h.appchat.R;
import com.devt3h.appchat.adapter.UserAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.SpaceTokenizer;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindFriendActivity extends AppCompatActivity {
    private UserAdapter adapterUser;
    private RecyclerView rvUser;
    private List<AccountUser> userList;
    private List<String> sUserName;
    private EditText edtKey;
    private ImageView imgBack;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        inits();
        edtKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String key = charSequence.toString();
                Query query = mDatabase.orderByChild(Constants.KEY_USER_NAME);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        sUserName.clear();
                        for (DataSnapshot task : dataSnapshot.getChildren()) {
                            AccountUser user = task.getValue(AccountUser.class);
                            if ( user.getName() != null && user.getName().toLowerCase().contains(key.toLowerCase())){
                                userList.add(user);
                            }
                        }
//
                        if(rvUser.getAdapter() == null){
                            adapterUser = new UserAdapter(new UserAdapter.IUser() {
                                @Override
                                public int getCount() {
                                    if (userList== null) return 0;
                                    return userList.size();
                                }

                                @Override
                                public AccountUser getUser(int position) {
                                    return userList.get(position);
                                }
                            }, false);

                            rvUser.setAdapter(adapterUser);
                        }else {
                            rvUser.getAdapter().notifyDataSetChanged();
                        }
                        //showCompleteStart(sUserName, userList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtKey.setOnKeyListener((v, keyCode, event)->{
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                searchFriend(edtKey.getText().toString());
                return true;
            }
            return false;
        });

        imgBack.setOnClickListener(view -> {
            closeSearch();
            //finish();
            //onBackPressed();
        });
    }

    private void inits() {
        edtKey = findViewById(R.id.edt_key);
        imgBack = findViewById(R.id.img_back);

        rvUser = findViewById(R.id.rv_find_user);
        rvUser.setLayoutManager(new LinearLayoutManager(this));


        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS);
        userList = new ArrayList<>();
        sUserName = new ArrayList<>();
    }

    private void closeSearch(){
        Intent intent = new Intent(FindFriendActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void searchFriend(final String key){
        Query query = mDatabase.orderByChild(Constants.KEY_USER_NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot task : dataSnapshot.getChildren()) {
                    AccountUser user = task.getValue(AccountUser.class);
                    if ( user.getName() != null && user.getName().toLowerCase().contains(key.toLowerCase())){
                        userList.add(user);
                    }

                }
                if(rvUser.getAdapter() == null){
                    adapterUser = new UserAdapter(new UserAdapter.IUser() {
                        @Override
                        public int getCount() {
                            if (userList== null) return 0;
                            return userList.size();
                        }

                        @Override
                        public AccountUser getUser(int position) {
                            return userList.get(position);
                        }
                    }, false);

                    rvUser.setAdapter(adapterUser);
                }else {
                    rvUser.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUser(String key){
        Query query = mDatabase.orderByChild(Constants.KEY_USER_NAME).startAt(key).endAt(key);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                userList.clear();
                if (adapterUser!=null) adapterUser.notifyDataSetChanged();
                AccountUser user = dataSnapshot.getValue(AccountUser.class);
                if(user!=null) userList.add(user);

                adapterUser = new UserAdapter(new UserAdapter.IUser() {
                    @Override
                    public int getCount() {
                        if (userList== null) return 0;
                        return userList.size();
                    }

                    @Override
                    public AccountUser getUser(int position) {
                        return userList.get(position);
                    }
                }, false);

                rvUser.setAdapter(adapterUser);
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
    public void onBackPressed() {
        super.onBackPressed();
        closeSearch();
    }
}
