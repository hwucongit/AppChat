package com.devt3h.appchat.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.devt3h.appchat.model.AccountUser;
import com.devt3h.appchat.model.User;
import com.google.android.gms.tasks.Continuation;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int AVATAR_GALLERY_PICK = 1;
    private final static int COVER_GALLERY_PICK = 2;

    private CircleImageView imgAvatar;
    private ImageView imgCover;
    private EditText edtUsername, edtEmail, edtPassword, edtStatus;
    private Button btnChange;
    private Toolbar toolbar;
    private Uri linkIAvartar, linkICover;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private StorageReference sReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        inits();
        sReference = FirebaseStorage.getInstance().getReference();
        firebaseUser = mAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        //edtEmail.setHint(firebaseUser.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS).child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AccountUser user = dataSnapshot.getValue(AccountUser.class);
                if(!user.getAvatarURL().equals(Constants.KEY_DEFAULT)){
                    String url = user.getAvatarURL();
                    Picasso.get().load(url)
                            .centerCrop()
                            .resize(getResources().getDimensionPixelSize(R.dimen.size_image),
                                    getResources().getDimensionPixelSize(R.dimen.size_image))
                            .into(imgAvatar);
                }

                if(!user.getCoverURL().equals(Constants.KEY_DEFAULT)){
                    String url = user.getCoverURL();
                    Picasso.get().load(url)
                            .centerCrop()
                            .resize(getResources().getDimensionPixelSize(R.dimen.size_image_cover),
                                    getResources().getDimensionPixelSize(R.dimen.size_image_cover))
                            .into(imgCover);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnChange.setOnClickListener(view->{
            String newUserName = edtUsername.getText().toString();
            String newEmail = edtEmail.getText().toString();
            String newPassword = edtPassword.getText().toString();
            String newStatus = edtStatus.getText().toString();

            if(!newPassword.isEmpty() && newPassword.length() < 6){
                Helper.showToast(SettingActivity.this,"Mật khẩu mới phải lớn hơn 6 ký tự");
            }else {
                dialogConformChange(newUserName,newPassword, newEmail, linkIAvartar, linkICover, newStatus);
               // changeProfile(newUserName ,newEmail,newPassword, linkIAvartar, linkICover, newStatus);
            }
        });

        imgAvatar.setOnClickListener(this);
        imgCover.setOnClickListener(this);
    }


    private void dialogConformChange(String newUserName,String newPassword, String newEmail, Uri linkIAvartar, Uri linkICover, String newStatus) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_conform_change_profile);
        dialog.setCanceledOnTouchOutside(false);

        Button btnConform = dialog.findViewById(R.id.btn_submit);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        btnConform.setOnClickListener(view ->{
            changeProfile(newUserName ,newEmail,newPassword, linkIAvartar, linkICover, newStatus);
            Helper.showToast(this, getResources().getString(R.string.setting_successeful));
            dialog.dismiss();
        });
        if(!newUserName.isEmpty() || !newPassword.isEmpty() || !newEmail.isEmpty() || !newStatus.isEmpty()
                || linkIAvartar!=null || linkICover!=null){
            dialog.show();
        }else {
            Helper.showToast(this, getResources().getString(R.string.not_change));
        }
    }

    private void changeProfile(String newUserName, String newEmail, String newPassword, Uri linkIAvatar, Uri linkICover, String status) {
        String userId = firebaseUser.getUid();
        if(!newEmail.isEmpty())  firebaseUser.updateEmail(newEmail);
        if(!newPassword.isEmpty()) firebaseUser.updatePassword(newPassword);

        if(linkIAvatar!=null){
            StorageReference filePathAvatar = sReference.child(Constants.STG_IMAGE).child(userId +".jpg");
            loadImg(filePathAvatar, Constants.KEY_AVATAR_URL, linkIAvatar);
        }

        if(linkICover!=null){
            StorageReference filePathCover = sReference.child(Constants.STG_COVER).child(userId +".jpg");
            loadImg(filePathCover, Constants.KEY_COVER_URL, linkICover);
        }

        if(!newUserName.isEmpty()) databaseReference.child(Constants.KEY_USER_NAME).setValue(newUserName);
        if(!status.isEmpty()) databaseReference.child(Constants.KEY_STATUS).setValue(status);
    }

    private void loadImg(StorageReference filePath, String key, Uri linkImg){
        UploadTask uploadTask = filePath.putFile(linkImg);
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> filePath.getDownloadUrl()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                databaseReference.child(key).setValue(downloadUri.toString());
                //Helper.showToast(SettingActivity.this, getResources().getString(R.string.setting_successeful));
            } else {
                // Handle failures
                // ...
                Helper.showToast(SettingActivity.this, getResources().getString(R.string.setting_error));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && data!=null){
            switch (requestCode){
                case AVATAR_GALLERY_PICK:
                    CropImage.activity(data.getData())
                            .start(this);
                    break;
                case COVER_GALLERY_PICK:
                    linkICover = data.getData();
                    imgCover.setImageURI(linkICover);
                    break;
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                linkIAvartar = result.getUri();
                imgAvatar.setImageURI(linkIAvartar);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void inits(){
        imgAvatar = findViewById(R.id.img_avatar);
        edtUsername = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnChange = findViewById(R.id.btn_change_profile);
        imgCover = findViewById(R.id.img_cover);
        edtStatus = findViewById(R.id.edt_status);

        toolbar = findViewById(R.id.setting_account);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.setting_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.img_avatar:
                Intent avatarIntent = new Intent();
                avatarIntent.setAction(Intent.ACTION_GET_CONTENT);
                avatarIntent.setType("image/*");
                startActivityForResult(avatarIntent, AVATAR_GALLERY_PICK);
                break;
            case R.id.img_cover:
                Intent coverIntent = new Intent();
                coverIntent.setAction(Intent.ACTION_GET_CONTENT);
                coverIntent.setType("image/*");
                startActivityForResult(coverIntent, COVER_GALLERY_PICK);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
