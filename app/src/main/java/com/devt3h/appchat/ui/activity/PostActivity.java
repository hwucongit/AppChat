package com.devt3h.appchat.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.devt3h.appchat.R;
import com.devt3h.appchat.databinding.ActivityPostBinding;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int GALLERY_CODE = 20;
    private ActivityPostBinding binding;
    private DatabaseReference userDatabase, postDatabase;
    private String idUserCurrent;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private Uri linkImg, downloadUrl;
    private StorageReference imgeReference;
    private ProgressDialog progressDialog;
    private Calendar calForData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_post);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post);
        initData();
    }

    private void initData() {
        userDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS);
        postDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_POST);
        idUserCurrent = FirebaseAuth.getInstance().getUid();

        imgeReference = FirebaseStorage.getInstance().getReference();

        userDatabase.child(idUserCurrent).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AccountUser user = dataSnapshot.getValue(AccountUser.class);
                if(!user.getAvatarURL().equals(Constants.KEY_DEFAULT)){
                    Picasso.get().load(user.getAvatarURL())
                            .centerCrop()
                            .resize(50,
                                    50)
                            .into(binding.imgAvatar);
                }

                binding.tvUsername.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.add_new_post));
        progressDialog.setMessage(getString(R.string.wait_update_post));


        binding.imgClose.setOnClickListener(this);
        binding.imgChoseImg.setOnClickListener(this);
        binding.tvShare.setOnClickListener(this);
        binding.imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String edtCaption = binding.edtCaption.getText().toString();
        int id = view.getId();
        switch (id){
            case R.id.tv_share:
                sharePost(edtCaption, linkImg);
                break;
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.img_chose_img:
                Intent iImg = new Intent();
                iImg.setAction(Intent.ACTION_GET_CONTENT);
                iImg.setType("image/*");
                startActivityForResult(iImg, GALLERY_CODE);
                break;
            case R.id.img_close:
                removeImage();
                break;
            default:
                break;
        }
    }

    private void removeImage() {
        binding.imgPicture.setImageDrawable(null);
        linkImg = null;
        binding.cvClose.setVisibility(View.INVISIBLE);
    }

    private void sharePost(String edtCaption, Uri linkImg) {
        if(edtCaption.isEmpty() && linkImg==null){
            return;
        }else{
            calForData = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            saveCurrentDate = format.format(calForData.getTime());

            SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
            saveCurrentTime = format1.format(calForData.getTime());

            SimpleDateFormat format2 = new SimpleDateFormat("ddMMyy_HHmm");
            postRandomName = format2.format(calForData.getTime()) + idUserCurrent;

            addPostToData(edtCaption, linkImg);
        }
    }

    private void addPostToData(String edtCaption, Uri linkImg) {
        if(linkImg==null){
            progressDialog.show();
            savePostToData(linkImg);
        }else{
            progressDialog.show();
            savePictureToStorage(linkImg);
        }
    }

    private void savePictureToStorage(Uri linkImg) {

        StorageReference filePath = imgeReference.child("Post Image").child(postRandomName +".jpg");
        loadImg(filePath, linkImg);

    }

    private void loadImg(StorageReference filePath, Uri linkImg){
        UploadTask uploadTask = filePath.putFile(linkImg);
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> filePath.getDownloadUrl()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadUrl = task.getResult();

                savePostToData(downloadUrl);
                //Helper.showToast(SettingActivity.this, getResources().getString(R.string.setting_successeful));
            } else {
                // Handle failures
                // ...
                Helper.showToast(PostActivity.this, getResources().getString(R.string.setting_error));
            }
        });
    }

    private void savePostToData(Uri downloadUrl) {
        String edtCaption = binding.edtCaption.getText().toString();
        userDatabase.child(idUserCurrent).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String userName = dataSnapshot.child(Constants.KEY_USER_NAME).getValue().toString();
                    String avatarURL = dataSnapshot.child(Constants.KEY_AVATAR_URL).getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", idUserCurrent);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);

                    if(edtCaption.isEmpty()) postMap.put("caption", Constants.KEY_DEFAULT);
                    else postMap.put("caption", edtCaption);

                    if(downloadUrl==null) postMap.put("postImage", Constants.KEY_DEFAULT);
                    else postMap.put("postImage", downloadUrl.toString());

                    postMap.put("avatarURL", avatarURL);
                    postMap.put("name", userName);

                    postDatabase.child(postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    //SendUserToMainActivity();
                                    //Helper.showToast(PostActivity.this, "New post is updated successfully");
                                    progressDialog.dismiss();

                                }else{
                                    //Helper.showToast(PostActivity.this, "Error occured!");
                                    progressDialog.dismiss();
                                }
                                //Helper.showToast(PostActivity.this, "New post is updated successfully");
                                progressDialog.dismiss();
                            });
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode == GALLERY_CODE && data!=null){
            linkImg = data.getData();
            binding.imgPicture.setImageURI(linkImg);
            binding.cvClose.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
