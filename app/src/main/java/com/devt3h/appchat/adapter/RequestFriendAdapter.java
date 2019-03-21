package com.devt3h.appchat.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.model.User;
import com.devt3h.appchat.ui.fragment.AddFriendRequestFragment;
import com.squareup.picasso.Picasso;

public class RequestFriendAdapter extends RecyclerView.Adapter<RequestFriendAdapter.RequestHolder> {
    private IRequestFriend iRequestFriend;

    public RequestFriendAdapter(IRequestFriend iRequestFriend) {
        this.iRequestFriend = iRequestFriend;
    }

    @NonNull
    @Override
    public RequestFriendAdapter.RequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.item_request_friend, viewGroup, false);
        return new RequestHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestFriendAdapter.RequestHolder requestHolder, final int position) {
        User user = iRequestFriend.getFriend(position);
        requestHolder.tvUsername.setText(user.getName());
        String url = user.getAvatarURL();
        if(url!= null && !url.equals(Constants.KEY_DEFAULT)){
            Picasso.get().load(url)
                    .resize(50, 50)
                    .centerCrop()
                    .into(requestHolder.imgAvatar);
        }else {
            Picasso.get()
                    .load(R.drawable.default_profile)
                    .resize(50, 50)
                    .centerCrop()
                    .into(requestHolder.imgAvatar);
        }
        requestHolder.btnAccepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iRequestFriend.acceptRequestFriend(position);
            }
        });

        requestHolder.btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iRequestFriend.declineRequestFriend(position);
            }
        });

        requestHolder.imgAvatar.setOnClickListener(view -> {
            //do something
            iRequestFriend.showDetailUser(position);
        });
    }

    @Override
    public int getItemCount() {
        return iRequestFriend.getCount();
    }

    public class RequestHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvUsername;
        private Button btnAccepted, btnDecline;
        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnAccepted = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }

    public interface IRequestFriend{
        int getCount();
        User getFriend(int position);

        void acceptRequestFriend(int postion);

        void declineRequestFriend(int position);

        void showDetailUser(int position);
    }
}
