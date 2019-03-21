package com.devt3h.appchat.ui.fragment;

import android.app.Activity;
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
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AcceptDeclineFragment extends Fragment implements View.OnClickListener {
    private Button btnAccept, btnDecline;
    private String key;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Constants.ARG_FRIENDS);
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_receive_request, container, false);
        btnAccept = v.findViewById(R.id.btn_accept);
        btnDecline = v.findViewById(R.id.btn_decline_request);

        key = getArguments().getString("key");

        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_accept:
                acceptedRequest(key);
                break;
            case R.id.btn_decline_request:
                declineRequest(key);
                break;
            default:
                break;
        }

    }

    private void declineRequest(String key) {
        Task task = mDatabase.child(key).removeValue();
        if(task.isSuccessful()){
            Helper.showToast(getContext(), getResources().getString(R.string.decline_done));
        }else{
            Helper.showToast(getContext(), getResources().getString(R.string.setting_error));
        }
    }

    private void acceptedRequest(String key) {

        mDatabase.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Task task = mDatabase.child(key).child(Constants.KEY_STATUS).setValue(Constants.KEY_ACCEPTED);
                Intent intent = new Intent();
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
//                if(task.isSuccessful()){
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
