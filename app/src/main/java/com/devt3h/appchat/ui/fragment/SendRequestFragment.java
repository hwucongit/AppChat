package com.devt3h.appchat.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.Friend;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendRequestFragment extends Fragment implements View.OnClickListener {
    private Button btnSendRequest;
    private String userId;

    private DatabaseReference mDatabasae = FirebaseDatabase.getInstance().getReference(Constants.ARG_FRIENDS);
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_request, container, false);
        btnSendRequest = v.findViewById(R.id.btn_send_request);

        userId = getArguments().getString("user_id");

        btnSendRequest.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        sendRequest();
        getActivity().finish();
    }

    private void sendRequest() {
        Friend friend = new Friend();
        friend.setSender_id(mAuth.getUid());
        friend.setReceiver_id(userId);
        friend.setStatus(Constants.KEY_PENDING);
        Task task = mDatabasae.push().setValue(friend);

        if(task.isSuccessful()){
            Helper.showToast(getContext(), getResources().getString(R.string.send_request_done));
        }else {
            Helper.showToast(getContext(), getResources().getString(R.string.setting_error));
        }
    }
}
