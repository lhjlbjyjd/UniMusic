package ua.rodionov.unimusic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class mediaPlayerService extends Service{

    MyBinder binder = new MyBinder();

    MainActivity mainActivity;
    String TAG = "MUSIC";
    long dur;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    TextView playngSongName;
    boolean playerActive = false;
    ArrayList<song> songs;
    mediaPlayerService service;
    mediaPlayerControlBar frag1;
    int position;
    public MediaPlayer mp = new MediaPlayer();
    public MediaPlayer previewMP = new MediaPlayer();
    private static final String VK_PATH = Environment.getExternalStorageDirectory().getPath() + "/.vkontakte/cache/audio/";
    private static final String UNI_PATH = Environment.getExternalStorageDirectory().getPath() + "/.UniMusic/audio/";
    private boolean shuffleActive = false;
    private int loop = 0;
    boolean songInterrupted = false;
    SharedPreferences sPref;
    Receiver rec = new Receiver();
    SharedPreferences.Editor editor;
    Random rand = new Random();
    AudioManager am;
    String previewSongName = "";
    AudioManager.OnAudioFocusChangeListener afChangeListener;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private static final int NOTIFICATION_ID = 84221;
    NotificationCompat.Builder mBuilder;
    String source;

    public static final String ACTION_PLAY = "ua.rodionov.unimusic.ACTION_PLAY";

    public static final String ACTION_PREVIOUS = "ua.rodionov.unimusic.ACTION_PREVIOUS";

    public static final String ACTION_NEXT = "ua.rodionov.unimusic.ACTION_NEXT";


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICE", "MyService onBind");
        service = this;
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        rec.mService = this;

        am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        afChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            pausePlayer();
                            songInterrupted = true;
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            if(songInterrupted){
                                resumePlayer();
                                songInterrupted = false;
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            am.abandonAudioFocus(afChangeListener);
                            pausePlayer();
                        }
                    }
                };


        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextSong();
            }
        });
        Log.d("SERVICE", "MyService onCreate");
    }

    public class MyBinder extends Binder{
        public mediaPlayerService getService() {
            Log.d("SERVICE", "MyService returned");
            return service;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ua.rodionov.unimusic.ACTION_PLAY");
        filter.addAction("ua.rodionov.unimusic.ACTION_PREVIOUS");
        filter.addAction("ua.rodionov.unimusic.ACTION_NEXT");
        //filter.addCategory("ua.rodionov.unimusic");
        this.registerReceiver(rec, filter);
        Log.d("SERVICE", "STARTED2");
        return START_STICKY;
    }

    public void setMainActivity(MainActivity ma){
        mainActivity = ma;

        sPref = mainActivity.getPreferences(MODE_PRIVATE);
        editor = sPref.edit();
        loop = sPref.getInt("LOOP", 0);
        shuffleActive = sPref.getBoolean("SHUFFLE", false);

        frag1 = (mediaPlayerControlBar) mainActivity.getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
        playngSongName = (TextView)frag1.getView().findViewById(R.id.activeSongName);
    }

    public void startPreviewPlayer(String url, String name){
        previewSongName = name;
        try {
            if(mp.isPlaying()){
                mp.pause();
            }
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                previewMP.reset();
                previewMP.setDataSource(url);
                previewMP.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity
                        .getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                if (bar != null && bar.getView() != null) {
                    ImageButton btn = (ImageButton) bar.getView().findViewById(R.id.playButton);
                    btn.setImageResource(R.drawable.ic_pause_black_48dp);
                }
                previewMP.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        previewMP.start();
                        dur = previewMP.getDuration();
                    }
                });
                previewMP.prepareAsync();
            }
        }catch(IOException ignore) {

        }
    }

    public void playerControl(ArrayList<song> _songs, int _position){
        if(previewMP.isPlaying()){
            previewMP.stop();
        }
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            RemoteControlWidget remoteViews = new RemoteControlWidget(getApplicationContext(), R.layout.notification);

            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContent(remoteViews);

            remoteViews.setViewVisibility(R.id.play_control, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.pause_control, View.VISIBLE);

            startForeground(NOTIFICATION_ID, mBuilder.build());

            Log.d("SERVICE", "1");
            position = _position;
            songs = _songs;

            playngSongName.setText(songs.get(position).getTitle());
            frag1.setProgressBar(0);

            Log.d("SERVICE", "2");
            switch (songs.get(position).getSource()){
                case 0:
                    try {
                        mp.reset();
                        mp.setDataSource(songs.get(position).data);
                        mp.prepare();
                        mp.start();
                        mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity
                                .getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                        if(bar != null && bar.getView() != null) {
                            ImageButton btn = (ImageButton) bar.getView().findViewById(R.id.playButton);
                            btn.setImageResource(R.drawable.ic_pause_black_48dp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        mp.reset();
                        mp.setDataSource(VK_PATH + (songs.get(position)).getName());
                        mp.prepare();
                        mp.start();
                        mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity
                                .getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                        if(bar != null && bar.getView() != null) {
                            ImageButton btn = (ImageButton) bar.getView().findViewById(R.id.playButton);
                            btn.setImageResource(R.drawable.ic_pause_black_48dp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        mp.reset();
                        mp.setDataSource(UNI_PATH + (songs.get(position)).getName());
                        mp.prepare();
                        mp.start();
                        mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity
                                .getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                        if(bar != null && bar.getView() != null) {
                            ImageButton btn = (ImageButton) bar.getView().findViewById(R.id.playButton);
                            btn.setImageResource(R.drawable.ic_pause_black_48dp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            String duration = songs.get(position).getDuration();
            Log.v("time", duration);
            dur = Long.parseLong(duration);

            Log.d("SERVICE", "3");

            if (mTimer != null) {
                mTimer.cancel();
            }

            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, 500, 500);
        }
    }

    public void playNextSong(){
        if(loop == 0 || loop == 2) {
            if (!shuffleActive) {
                position++;
                if (position == songs.size()) {
                    if(loop == 2) {
                        playerControl(songs, 0);
                    }else{
                        position = 0;
                        mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity
                                .getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                        bar.setProgressBar(0);
                        if(bar.getView() != null) {
                            ((ImageButton) bar.getView().findViewById(R.id.playButton)).setImageResource(R.drawable.ic_pause_black_48dp);
                        }
                        mp.stop();
                        am.abandonAudioFocus(afChangeListener);
                    }
                } else {
                    playerControl(songs, position);
                }
            } else {
                position = rand.nextInt(songs.size());
                playerControl(songs, position);
            }
        }else{
            playerControl(songs, position);
        }
    }

    public void playPrevSong(){
        if(mp.getCurrentPosition() <= 3000) {
            if (position-1 >= 0) {
                position--;
                playerControl(songs, position);
            } else {
                playerControl(songs, position);
            }
        }else{
            playerControl(songs, position);
        }
    }

    public void playSong(ArrayList<song> songs, int position){

    }

    public void setPlayerTime(int time){
        mp.seekTo(time);
        mp.start();
    }

    public int getPlayerTime(){
        return mp.getCurrentPosition();
    }

    public int getDuration(){
        return mp.getDuration();
    }

    public void pausePlayer(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteControlWidget remoteViews = new RemoteControlWidget(getApplicationContext(), R.layout.notification);

        stopForeground(false);

        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteViews);

        remoteViews.setViewVisibility(R.id.play_control, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.pause_control, View.INVISIBLE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        mp.pause();
    }

    public void resumePlayer() {
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            RemoteControlWidget remoteViews = new RemoteControlWidget(getApplicationContext(), R.layout.notification);

            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContent(remoteViews);

            remoteViews.setViewVisibility(R.id.play_control, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.pause_control, View.VISIBLE);

            startForeground(NOTIFICATION_ID, mBuilder.build());

            mp.start();
        }
    }

    public void setShuffle(boolean res){
        shuffleActive = res;
        editor.putBoolean("SHUFFLE", res);
        editor.commit();
    }

    public boolean getShuffle(){
        return shuffleActive;
    }

    public void setLooping(int res){
        loop = res;
        editor.putInt("LOOP", res);
        editor.commit();
    }

    public void setPlaylist(ArrayList<song> _songs){
        songs = _songs;
    }

    public int getLooping(){
        return loop;
    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaPlayerControlBar bar = (mediaPlayerControlBar) mainActivity.getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar);
                    try {
                        bar.setProgressBar((mp.getCurrentPosition() * 100) / dur);
                        if(previewMP.isPlaying() && position < songs.size()){
                            ((TextView) bar.getView().findViewById(R.id.activeSongName)).setText(previewSongName);
                        }else if(position < songs.size()){
                            ((TextView) bar.getView().findViewById(R.id.activeSongName)).setText(songs.get(position).getTitle());
                        }
                    }catch(NullPointerException ignored){

                    }
                }
            });
        }
    }

    public static class Receiver extends BroadcastReceiver{

        mediaPlayerService mService;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case ACTION_PLAY:
                    if(mService.songs != null) {
                        if (mService.mp.isPlaying()) {
                            mService.pausePlayer();

                        } else {
                            mService.resumePlayer();
                        }
                    }
                    break;
                case ACTION_PREVIOUS:
                    mService.playPrevSong();
                    break;
                case ACTION_NEXT:
                    mService.playNextSong();
                    break;
            }
        }
    }

}
