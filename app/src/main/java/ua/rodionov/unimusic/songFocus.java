package ua.rodionov.unimusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Дмитрий on 29.05.2016.
 */
public class songFocus extends AppCompatActivity{

    mediaPlayerService service = null;
    final String LOG_TAG = "myLogs";
    mediaPlayerService.MyBinder binder;
    boolean bound = false, touch = false, playerActive = false;
    int songDuration;
    SeekBar bar;
    RelativeLayout controlsLayout;
    ImageView songCover;
    ImageButton playButton, nextSongButton, prevSongButton, shuffleButton, loopButton, stopServiceButton;
    TextView activeSongName, activeSongArtist;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_focus);

        songCover = (ImageView)findViewById(R.id.songCover);
        controlsLayout = (RelativeLayout) findViewById(R.id.controlsLayout);
        bar = (SeekBar)findViewById(R.id.seekBar);
        playButton = (ImageButton)findViewById(R.id.playButton);
        nextSongButton = (ImageButton)findViewById(R.id.nextSongButton);
        stopServiceButton = (ImageButton)findViewById(R.id.stopService);
        prevSongButton = (ImageButton)findViewById(R.id.prevSongButton);
        activeSongName = (TextView)findViewById(R.id.activeSongName);
        activeSongArtist = (TextView)findViewById(R.id.activeSongArtist);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        //AdMob Google

        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-3872207617963522~9202780494");

        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView);

        AdRequest request = new AdRequest.Builder()
                .build();
        adView.loadAd(request);*/


        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //adView.setLayoutParams(new RelativeLayout.LayoutParams(width, (height / 2) - 30));
            songCover.setLayoutParams(new RelativeLayout.LayoutParams(width, (height / 2) - 30));
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(width, (height / 2) - 10);
            p.addRule(RelativeLayout.BELOW, R.id.songCover);
            controlsLayout.setLayoutParams(p);
        }else{
            //adView.setLayoutParams(new RelativeLayout.LayoutParams(width/2, height));
            songCover.setLayoutParams(new RelativeLayout.LayoutParams(width/2, height));
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(width/2, height);
            p.addRule(RelativeLayout.RIGHT_OF, R.id.songCover);
            controlsLayout.setLayoutParams(p);
            RelativeLayout.LayoutParams barParams = new RelativeLayout.LayoutParams(width/2, getPixels(15));
            barParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
            barParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            barParams.setMargins(getPixels(-10),0,getPixels(-10),getPixels(10));
            bar.setLayoutParams(barParams);
        }

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                service.mp.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                service.mp.pause();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    service.setPlayerTime(progress);
                }
            }
        });

        nextSongButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                service.playNextSong();
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                service.stopForeground(true);
            }
        });

        prevSongButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                service.playPrevSong();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if(!playerActive) {
                    service.mp.start();
                    playButton.setImageResource(R.drawable.ic_pause_black_48dp);
                    playerActive = !playerActive;
                }else{
                    service.mp.pause();
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                    playerActive = !playerActive;
                }
            }
        });

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 500, 500);
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(this, mediaPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /*@Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(mConnection);
    }*/

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder srv) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mediaPlayerService.MyBinder binder = (mediaPlayerService.MyBinder) srv;
            service = binder.getService();
            songDuration = service.getDuration();
            bar.setMax(songDuration);
            bar.setProgress(service.getPlayerTime());
            activeSongName.setText(service.songs.get(service.position).getTitle());
            activeSongArtist.setText(service.songs.get(service.position).getArtist());
            setButtons();
            if(service.mp.isPlaying()){
                playButton.setImageResource(R.drawable.ic_pause_black_48dp);
            }
            playerActive = service.mp.isPlaying();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void setButtons(){
        shuffleButton = (ImageButton)findViewById(R.id.shuffleButton);
        loopButton = (ImageButton)findViewById(R.id.loopButton);

        if(service.getShuffle()){
            shuffleButton.setImageResource(R.drawable.shuffle);
        }else{
            shuffleButton.setImageResource(R.drawable.shuffle_inact);
        }

        switch (service.getLooping()){
            case 0:
                loopButton.setImageResource(R.drawable.loop_inact);
                break;
            case 1:
                loopButton.setImageResource(R.drawable.loop_one);
                break;
            case 2:
                loopButton.setImageResource(R.drawable.loop);
                break;
        }

        shuffleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                service.setShuffle(!service.getShuffle());
                if(service.getShuffle()){
                    shuffleButton.setImageResource(R.drawable.shuffle);
                }else{
                    shuffleButton.setImageResource(R.drawable.shuffle_inact);
                }
            }
        });
        loopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (service.getLooping()){
                    case 0:
                        service.setLooping(1);
                        loopButton.setImageResource(R.drawable.loop_one);
                        break;
                    case 1:
                        service.setLooping(2);
                        loopButton.setImageResource(R.drawable.loop);
                        break;
                    case 2:
                        service.setLooping(0);
                        loopButton.setImageResource(R.drawable.loop_inact);
                        break;
                }
            }
        });
    }

    public int getPixels(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) dp, getResources().getDisplayMetrics());
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return service;
    }


    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activeSongName.setText(service.songs.get(service.position).getTitle());
                    activeSongArtist.setText(service.songs.get(service.position).getArtist());
                    bar.setProgress(service.getPlayerTime());
                }
            });
        }
    }
}
