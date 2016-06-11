package ua.rodionov.unimusic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class mediaPlayerService extends Service{

    MyBinder binder = new MyBinder();

    MainActivity mainActivity;
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
    private static final String VK_PATH = Environment.getExternalStorageDirectory().getPath() + "/.vkontakte/cache/audio/";
    private boolean shuffleActive = false;
    private int loop = 0;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    Random rand = new Random();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICE", "MyService onBind");
        service = this;
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Скоро тут будет управление")
                        .setContentText("Отвечаю!");
        Intent resultIntent = new Intent(this, songFocus.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        startForeground(256781,mBuilder.build());*/
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
        //Write functions here
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

    public void startPreviewPlayer(String url){
        try {
            mp.reset();
            mp.setDataSource(url);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mp.prepareAsync();
        }catch(IOException ignore) {

        }
    }

    public void playerControl(ArrayList<song> _songs, int _position){
        Log.d("SERVICE", "1");
        position = _position;
        songs = _songs;

        playngSongName.setText(songs.get(position).getTitle());
        frag1.setProgressBar(0);

        Log.d("SERVICE", "2");
        if(songs.get(position).getSource() == 1) {
            try {
                mp.reset();
                mp.setDataSource(VK_PATH + (songs.get(position)).getName());
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                mp.reset();
                mp.setDataSource(songs.get(position).data);
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void playNextSong(){
        if(loop == 0 || loop == 2) {
            if (!shuffleActive) {
                position++;
                if (position == songs.size()) {
                    playerControl(songs, 0);
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
        mp.pause();
        playerActive = true;
    }

    public void resumePlayer() {
        mp.start();
        playerActive = false;
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
                        ((TextView) bar.getView().findViewById(R.id.activeSongName)).setText(songs.get(position).getTitle());
                    }catch(NullPointerException ignored){

                    }
                }
            });
        }
    }

}
