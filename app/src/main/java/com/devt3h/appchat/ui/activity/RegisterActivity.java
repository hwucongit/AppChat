package com.devt3h.appchat.ui.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.devt3h.appchat.R;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.helper.Helper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnRegister;
    private Toolbar toolbar;
    private EditText edtName, edtEmail, edtPassword, edtRetypePassword, edtBirthday;
    private EditText edtCareer, edtCountry;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private ProgressDialog progressRegisterDialog;

    private Calendar myCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        inits();
    }
    private void inits(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtRetypePassword = findViewById(R.id.edt_retype_password);
        edtCareer = findViewById(R.id.edt_career);
        edtCountry = findViewById(R.id.edt_country);

        edtBirthday = findViewById(R.id.edt_birthday);
        setDatePicker();

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        progressRegisterDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        String name = edtName.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        String retype_password = edtRetypePassword.getText().toString();
        String birthday = edtBirthday.getText().toString();
        String career = edtCareer.getText().toString();
        String country = edtCountry.getText().toString();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || retype_password.isEmpty() || birthday.isEmpty()){
            Helper.showToast(RegisterActivity.this,"Bạn phải điền đầy đủ thông tin");
        }else if(!password.equals(retype_password)){
            Helper.showToast(RegisterActivity.this,"Hai mật khẩu phải giống nhau");
        }else if(password.length() < 6){
            Helper.showToast(RegisterActivity.this,"Mật khẩu phải lớn hơn 6 ký tự");
        }else {
            register(name,email,password, birthday, career, country);
        }

    }
    private void register(final String name, String email, String password, final String birthday, String career, String country){
        progressRegisterDialog.setTitle(getString(R.string.creating_new_account));
        progressRegisterDialog.setMessage(getString(R.string.please_wait));
        progressRegisterDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String currentUserId = currentUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", currentUserId);
                            hashMap.put(Constants.KEY_USER_NAME, name);
                            hashMap.put(Constants.KEY_AVATAR_URL, Constants.KEY_DEFAULT);
                            hashMap.put(Constants.KEY_COVER_URL, Constants.KEY_DEFAULT);
                            hashMap.put(Constants.KEY_BIRTHDAY, birthday);
                            hashMap.put(Constants.KEY_CAREER, career);
                            hashMap.put(Constants.KEY_COUNTRY, country);
                            hashMap.put(Constants.KEY_STATUS,Constants.KEY_DEFAULT);

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }else {
                            Helper.showToast(RegisterActivity.this,getString(R.string.fail_register));
                        }
                        progressRegisterDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
    }

    private void setDatePicker(){
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        edtBirthday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(RegisterActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        edtBirthday.setText(sdf.format(myCalendar.getTime()));
    }
}
