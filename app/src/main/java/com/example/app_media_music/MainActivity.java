package com.example.app_media_music;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    
    public static final String TAG = "BBB";
    public static final int PLAYER_SERVICE_NOTIFICATION_ID = 1;
    @BindView(R.id.txtTenbaihat)
    TextView txtTenbaihat;
    @BindView(R.id.ImgeView)
    ImageView ImgeView;
    @BindView(R.id.ImgCardview)
    CardView ImgCardview;
    @BindView(R.id.txtTimeBegin)
    TextView txtTimeBegin;
    @BindView(R.id.txtTimeEnd)
    TextView txtTimeEnd;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.ImgPre)
    ImageView ImgPre;
    @BindView(R.id.ImgPlay)
    ImageView ImgPlay;
    @BindView(R.id.ImgStop)
    ImageView ImgStop;
    @BindView(R.id.ImgNext)
    ImageView ImgNext;
    ObjectAnimator mAnimator;
    ServiceConnection serviceConnection;
    BoundService boundService;
    int stateAnimation = 0, stateSong = 0;
    boolean stateNotication = true;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("actionName");
            switch (action) {
                case BoundService.ACTION_PLAY:
                    if (boundService.isPlaying()) {
                        ImgPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                        mAnimator.pause();
                    } else {
                        ImgPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                        mAnimator.resume();
                    }
                    /*
                    Anh tự thêm vô các trường hợp nha (nhớ là nó còn ở bên BoundService nữa á)
                case ACTION_NEXT:
                    break;
                case ACTION_PREVIOUS:
                    break;*/
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d("BBB", getIntent().toString());
        AnimationRotate();
        Connect();
        Click();
        registerReceiver(broadcastReceiver, new IntentFilter("NEXTBR"));
    }

    @Override
    protected void onStart() {
        Intent intent = new Intent(this, BoundService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getExtras().getString("actionName");
//                if(BoundService.ACTION_NEXT.equals(action)){
//                    boundService.Next();
//                    Toast.makeText(context, "aaaa", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
        super.onStart();
    }

    public void Connect() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BoundService.LocalBinder localBinder = (BoundService.LocalBinder) service;
                boundService = localBinder.getService();
                boundService.addSong();
                boundService.CreateMedia();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
    }

    public void Click() {
        ImgPlay.setOnClickListener(v -> {
            if (stateNotication == true) {
                Notification notification = boundService.CreateNotification(10).build();
                NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(PLAYER_SERVICE_NOTIFICATION_ID, notification);
                boundService.Play();
                stateNotication = false;
            } else {
                boundService.Play();
            }
            if (boundService.mStateImg == 1) {
                if (stateAnimation == 0) {
                    mAnimator.start();
                    stateAnimation = 1;
                }
                if (stateAnimation == 2) {
                    mAnimator.resume();
                    stateAnimation = 1;
                }
                ImgPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            } else if (boundService.mStateImg == 0) {
                if (stateAnimation == 1) {
                    mAnimator.pause();
                    stateAnimation = 2;
                }
                ImgPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            }
            txtTenbaihat.setText(boundService.getTitle());
            boundService.TimeEnd();
            txtTimeEnd.setText(boundService.getTimeEnd());

        });

        ImgStop.setOnClickListener(v -> {
            boundService.Stop();
            boundService.CreateMedia();
            ImgPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        });

        ImgNext.setOnClickListener(v -> {
            boundService.Next();
            ChonHinh();
            ImgPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            txtTenbaihat.setText(boundService.getTitle());
            boundService.TimeEnd();
            txtTimeEnd.setText(boundService.getTimeEnd());
            Notification notification = boundService.CreateNotification(10).build();
            NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(PLAYER_SERVICE_NOTIFICATION_ID, notification);
        });

        ImgPre.setOnClickListener(v -> {
            stateSong = 2;
            boundService.Previous();
            ChonHinh();
            ImgPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            txtTenbaihat.setText(boundService.getTitle());
            boundService.TimeEnd();
            txtTimeEnd.setText(boundService.getTimeEnd());
            Notification notification = boundService.CreateNotification(10).build();
            NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(PLAYER_SERVICE_NOTIFICATION_ID, notification);
        });
    }

    public void ChonHinh() {
        if (boundService.mPosition == 0)
            ImgeView.setBackgroundResource(R.drawable.img_loiyeu);
        if (boundService.mPosition == 1)
            ImgeView.setBackgroundResource(R.drawable.dungchoanhnua);
        if (boundService.mPosition == 2)
            ImgeView.setBackgroundResource(R.drawable.img_nuocmat);
    }

    public void AnimationRotate() {
        mAnimator = ObjectAnimator.ofFloat(ImgeView, View.ROTATION, 0f, 360f);
        mAnimator.setDuration(30000).setRepeatCount(Animation.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
    }



}
