package com.devt3h.appchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.Chat;
import com.devt3h.appchat.model.User;
import com.devt3h.appchat.ui.activity.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HistoryChatAdapter extends RecyclerView.Adapter<HistoryChatAdapter.HistoryChatViewHolder> {

    private List<User> mUsers;
    private Context context = null;
    private String lastMessage;

    public HistoryChatAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public HistoryChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_chat, viewGroup, false);
        return new HistoryChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryChatViewHolder historyChatViewHolder, int i) {
        User user = mUsers.get(i);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        historyChatViewHolder.tvUsername.setText(user.getName());
        String url = user.getAvatarURL();
        if (url != null && !url.equals(Constants.KEY_DEFAULT)) {
            Picasso.get().load(url)
                    .resize(50, 50)
                    .centerCrop()
                    .into(historyChatViewHolder.ivAvatar);
        } else {
            Picasso.get()
                    .load(R.drawable.default_profile)
                    .resize(50, 50)
                    .centerCrop()
                    .into(historyChatViewHolder.ivAvatar);
        }
        if(user.isOnline()){
            historyChatViewHolder.ivOnline.setVisibility(View.VISIBLE);
            historyChatViewHolder.ivOffline.setVisibility(View.GONE);
        }else {
            historyChatViewHolder.ivOnline.setVisibility(View.GONE);
            historyChatViewHolder.ivOffline.setVisibility(View.VISIBLE);
        }
        if(currentUser != null)
            loadLastMessage(user.getId(),historyChatViewHolder.tvLastMessage);

        historyChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mUsers == null)
            return 0;
        return mUsers.size();
    }

    public class HistoryChatViewHolder extends ViewHolder {
        private ImageView ivAvatar;
        private TextView tvUsername, tvLastMessage;
        private ImageView ivOnline, ivOffline;

        public HistoryChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.img_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            ivOnline = itemView.findViewById(R.id.imv_status_on);
            ivOffline = itemView.findViewById(R.id.imv_status_off);
        }
    }

    private void loadLastMessage(String userId,TextView tvLastMessage) {
        lastMessage = "default";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Chat chat = data.getValue(Chat.class);
                    if (currentUser.getUid().equals(chat.getSender_id()) && userId.equals(chat.getReceiver_id())
                            || currentUser.getUid().equals(chat.getReceiver_id()) && userId.equals(chat.getSender_id())) {
                        if(chat.getType().equals("text")) {
                            lastMessage = chat.getMessage();
                        }else if(chat.getType().equals("image")){
                            lastMessage = "Image";
                        }
                    }
                }
                tvLastMessage.setText(lastMessage);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
