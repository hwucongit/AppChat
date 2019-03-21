package com.devt3h.appchat.ui.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.devt3h.appchat.R;
import com.devt3h.appchat.adapter.ViewPagerAdapter;
import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.service.NotificationRequestFriend;
import com.devt3h.appchat.ui.fragment.AccountFragment;
import com.devt3h.appchat.ui.fragment.AddFriendRequestFragment;
import com.devt3h.appchat.ui.fragment.ChatsFragment;
import com.devt3h.appchat.ui.fragment.FriendsFragment;
import com.devt3h.appchat.ui.fragment.NewsFeedFragment;
import com.devt3h.appchat.ui.fragment.NotificationFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int OPEN_CAMERA = 1;
    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private EditText edtSearch;
    private ImageView imgCamera, imgLogout;
    private NotificationRequestFriend notificationService;
    private ServiceConnection conn;
    private String currentUserId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        startStartCommand();
        inits();
        initConnect();
    }
    private void startStartCommand() {
        Intent intent = new Intent();
        intent.setClass(this, NotificationRequestFriend.class);
        startService(intent);
    }

    private void initConnect() {
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                ((NotificationRequestFriend.MyBinder)iBinder).getNotificationRequestFriend().showNotification();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                notificationService = null;
            }
        };
        Intent intent = new Intent();
        intent.setClass(this, NotificationRequestFriend.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }


    private void showActivitySearch() {
        Intent searchIntent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(searchIntent);
    }

    private void inits() {

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        edtSearch = findViewById(R.id.edt_search);
        imgCamera = findViewById(R.id.img_camera);
        imgLogout = findViewById(R.id.img_logout);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new NewsFeedFragment());
        viewPagerAdapter.addFragment(new AddFriendRequestFragment());
        viewPagerAdapter.addFragment(new ChatsFragment());
        //viewPagerAdapter.addFragment(new NotificationFragment());
        viewPagerAdapter.addFragment(new AccountFragment());

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        initTabLayout();

        imgLogout.setOnClickListener(view -> {
            logOutUser();
        });

        imgCamera.setOnClickListener(view -> {
            Intent iCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(iCamera, OPEN_CAMERA);
        });

        //showActivitySearch();
        edtSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showActivitySearch();
                return true;
            }
        });

        imgCamera.setOnClickListener(view -> {
            Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(postIntent);
        });
    }

    private void initTabLayout(){
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_newsfeed_selected);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_friend);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_mess);
        //tabLayout.getTabAt(3).setIcon(R.drawable.ic_notification);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_account);

        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ffffff"));

        tabLayout.getTabAt(0).select();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position){
                    case 0:
                        tab.setIcon(R.drawable.ic_newsfeed_selected);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_friend_selected);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_mess_selected);
                        break;
//                    case 3:
//                        tab.setIcon(R.drawable.ic_notification_selected);
//                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_account_selected);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position){
                    case 0:
                        tab.setIcon(R.drawable.ic_newsfeed);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_friend);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_mess);
                        break;
//                    case 3:
//                        tab.setIcon(R.drawable.ic_notification);
//                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_account);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.btn_log_out:
                mAuth.signOut();
                logOutUser();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOutUser() {
        Dialog dialogLogOut = new Dialog(this);
        dialogLogOut.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLogOut.setContentView(R.layout.dialog_conform_log_out);
        dialogLogOut.setCanceledOnTouchOutside(false);
        dialogLogOut.show();

        Button btnConform = dialogLogOut.findViewById(R.id.btn_submit);
        Button btnCancel = dialogLogOut.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(view -> {
            dialogLogOut.dismiss();
        });

        btnConform.setOnClickListener(view->{
            mAuth.signOut();
            dialogLogOut.dismiss();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


    }
    public void setStatusOnline(boolean b){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS).child(currentUserId);
        Map<String,Object> map = new HashMap<>();
        map.put("online",b);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusOnline(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatusOnline(false);
    }
}
