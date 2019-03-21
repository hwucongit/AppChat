package com.devt3h.appchat.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.devt3h.appchat.R;
import com.devt3h.appchat.adapter.MessageAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.Chat;
import com.devt3h.appchat.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtMessage;
    private ImageButton btnSend, btnCallVoice, btnCallVideo, btnSelectImage;
    private TextView tvNameOfFriend;
    private RecyclerView rvMessage;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private StorageReference messageImgRef;
    private Toolbar toolbar;
    private List<Chat> listChat;
    private MessageAdapter messageAdapter;
    private SinchClient sinchClient;
    private Call call;
    private Context context;
    private static int PICK_IMAGE = 1;
    private String receiverId;
    private String senderId;
    private ProgressDialog loadingSendImageDialog;
    private Date date;
    private ValueEventListener seenMessageEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        inits();
        handleCall();
    }

    private void inits() {

        context = this;
        date = new Date();
        edtMessage = findViewById(R.id.edt_message);
        btnSend = findViewById(R.id.btn_send);
        rvMessage = findViewById(R.id.rv_message);
        tvNameOfFriend = findViewById(R.id.tv_user_chat_name);
        btnCallVoice = findViewById(R.id.btn_call_voice);
        btnCallVideo = findViewById(R.id.btn_call_video);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnCallVoice.setOnClickListener(this);
        btnCallVideo.setOnClickListener(this);
        btnSelectImage.setOnClickListener(this);
        loadingSendImageDialog = new ProgressDialog(context);

        receiverId = getIntent().getStringExtra("userId");
        senderId = FirebaseAuth.getInstance().getUid();

        messageImgRef = FirebaseStorage.getInstance().getReference().child("Message_Image");

        // toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // recycler view message
        LinearLayoutManager messageLayoutManager = new LinearLayoutManager(getApplicationContext());
        messageLayoutManager.setStackFromEnd(true);
        rvMessage.setHasFixedSize(true);
        rvMessage.setLayoutManager(messageLayoutManager);

        // chat
        final String userId = getIntent().getStringExtra("userId");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS).child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                tvNameOfFriend.setText(user.getName());
                readMessage(currentUser.getUid(), userId, user.getAvatarURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edtMessage.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(currentUser.getUid(), userId, message);
                } else {
                    Helper.showToast(getApplicationContext(), "Tin nhắn rỗng");
                }

            }
        });
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        seenMessage(receiverId);
    }

    private void sendMessage(String senderId, String receiverId, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender_id", senderId);
        hashMap.put("receiver_id", receiverId);
        hashMap.put("message", message);
        hashMap.put("type", "text");
        hashMap.put("seen", false);

        reference.child("Chats").push().setValue(hashMap);
        edtMessage.setText("");
    }

    private void readMessage(final String senderId, final String receiverId, final String avatarURL) {
        listChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listChat.clear();
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Chat chat = data.getValue(Chat.class);
                    if ((senderId.equals(chat.getSender_id()) && receiverId.equals(chat.getReceiver_id()))
                            || (senderId.equals(chat.getReceiver_id()) && receiverId.equals(chat.getSender_id()))) {
                        listChat.add(chat);
                    }
                }

                messageAdapter = new MessageAdapter(new MessageAdapter.IMessage() {
                    @Override
                    public int getCount() {
                        return listChat == null ? 0 : listChat.size();
                    }

                    @Override
                    public Chat getChat(int position) {
                        return listChat.get(position);
                    }

                    @Override
                    public String getAvatarURL() {
                        return avatarURL;
                    }
                });
                rvMessage.setAdapter(messageAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_call_voice:
                callUser(getIntent().getStringExtra("userId"));
                break;
            default:
                break;
        }

    }

    private void handleCall() {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .applicationKey("f5dfada3-94a5-432a-902d-82a235225cc2")
                .applicationSecret("n5S6IEurMEaL0t0LqgWUCw==")
                .environmentHost("sandbox.sinch.com")
                .userId(currentUser.getUid())
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        sinchClient.start();
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(getString(R.string.calling));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.pick_up), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    call = incomingCall;
                    call.answer();
                    call.addCallListener(new SinchCallListener());
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.reject), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    call.hangup();
                }
            });
            alertDialog.show();
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            Helper.showToast(getApplicationContext(), getString(R.string.ringging));
        }

        @Override
        public void onCallEstablished(Call call) {
            Helper.showToast(getApplicationContext(), getString(R.string.call_established));
        }

        @Override
        public void onCallEnded(Call endCall) {
            Helper.showToast(getApplicationContext(), getString(R.string.call_ended));
            call = null;
            endCall.hangup();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    public void callUser(String recipientId) {
        if (call == null) {
            call = sinchClient.getCallClient().callUser(recipientId);
            call.addCallListener(new SinchCallListener());
            showCallDialog(call);
        }
    }

    private void showCallDialog(Call call) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert));
        alertDialog.setMessage(getString(R.string.calling));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.reject), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                call.hangup();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            loadingSendImageDialog.setTitle(getString(R.string.sending_image));
            loadingSendImageDialog.setMessage(getString(R.string.please_wait));
            loadingSendImageDialog.show();
            Uri imageURI = data.getData();
            StorageReference filePath = messageImgRef.child(receiverId + date.toString() + ".jpg");
            UploadTask uploadTask = filePath.putFile(imageURI);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> filePath.getDownloadUrl()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String linkImage = task.getResult().toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender_id", senderId);
                    hashMap.put("receiver_id", receiverId);
                    hashMap.put("message", linkImage);
                    hashMap.put("type", "image");
                    hashMap.put("seen", false);

                    reference.child("Chats").push().setValue(hashMap);
                    loadingSendImageDialog.dismiss();
                } else {
                    Helper.showToast(context, getString(R.string.fail_send_image));
                    loadingSendImageDialog.dismiss();
                }

            });
        }
    }
    private void seenMessage(String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenMessageEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Chat chat = data.getValue(Chat.class);
                    if(chat.getReceiver_id().equals(currentUser.getUid())&& chat.getSender_id().equals(userId)){
                        Map<String, Object> map = new HashMap<>();
                        map.put("seen", true);
                        data.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS).child(senderId);
        Map<String,Object> map = new HashMap<>();
        map.put("online",true);
        reference.updateChildren(map);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS).child(senderId);
        Map<String,Object> map = new HashMap<>();
        map.put("online",false);
        reference.updateChildren(map);
        reference.removeEventListener(seenMessageEventListener);
    }
}
