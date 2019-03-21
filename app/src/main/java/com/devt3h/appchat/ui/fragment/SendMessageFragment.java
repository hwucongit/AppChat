package com.devt3h.appchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devt3h.appchat.R;
import com.devt3h.appchat.ui.activity.ChatActivity;

public class SendMessageFragment extends Fragment implements View.OnClickListener {
    private Button btnSendMessage;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_message,container,false);
        btnSendMessage = v.findViewById(R.id.btn_send_message);
        btnSendMessage.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(),ChatActivity.class);
        userId = getArguments().getString("user_id");
        intent.putExtra("userId",userId);
        startActivity(intent);
    }
}
