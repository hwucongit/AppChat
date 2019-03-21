package com.devt3h.appchat.com;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.devt3h.appchat.helper.Constants;
import com.devt3h.appchat.ui.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = MyApplication.class.getSimpleName();
    private boolean isUpdateStatusOnline;
    private Disposable disposable;
    private FirebaseUser userReference;
    private DatabaseReference databaseReference;
    private static long offsetFromUtc;

    public static long getOffsetFromUtc(){
        return offsetFromUtc;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        offsetFromUtc = tz.getOffset(now.getTime());
        registerActivityLifecycleCallbacks(this);
    }

    public void updateFirebaseUpdate() {
        userReference = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.ARG_USERS);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted.................");
        isUpdateStatusOnline = true;
        if (activity instanceof MainActivity) {
            updateFirebaseUpdate();
            updateStatusOnline();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped.................");
        if ( activity instanceof MainActivity){
            disConnect();
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public void disConnect() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    private void updateStatusOnline() {
        if (disposable != null && !disposable.isDisposed()) {
            return;
        }
        disposable = Observable.create((ObservableOnSubscribe<Boolean>) t -> {
            if (databaseReference != null) {
                if (CommonUtils.isOnline(MyApplication.this)) {
                    databaseReference.child(userReference.getUid()).child("lastUpdateStatus").setValue(
                            new Date().getTime() - offsetFromUtc
                    );
                }
            }
            t.onNext(true);
            t.onComplete();
        })
                .retryWhen(error -> error.delay(3, TimeUnit.SECONDS))
                .repeatWhen(complete -> complete.delay(3, TimeUnit.SECONDS))
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe();
    }
}
