package ua.rodionov.unimusic;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.util.VKUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    Tracks track = new Tracks();
    VKtracks vktracks = new VKtracks();
    DeviceTracks deviceTracks = new DeviceTracks();
    boolean serviceStarted = false, barVisible = true;
    Fragment frame;
    mediaPlayerControlBar frag1;
    mediaPlayerService service = null;
    ArrayList<song> songs = new ArrayList<>();
    int position;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    final String LOG_TAG = "myLogs";
    boolean bound = false, isBound = false, searchOpened = false;
    ServiceConnection sConn;
    //IInAppBillingService mService;
    mediaPlayerService.MyBinder binder;
    FirebaseAnalytics mFirebaseAnalytics;
    FloatingActionButton fab;
    Intent intent;
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private static final String VK_PATH = Environment.getExternalStorageDirectory().getPath() + "/.vkontakte/cache/audio/";
    private static final String UNI_PATH = Environment.getExternalStorageDirectory().getPath() + "/.UniMusic/audio/";
    InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if(!VKSdk.isLoggedIn()){
            VKSdk.login(this, VKScope.AUDIO);
        }

        File file = new File(UNI_PATH);

        View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        if(Build.VERSION.SDK_INT >= 23 && coordinatorLayoutView != null) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(coordinatorLayoutView,
                            "Как, по твоему, я должен открыть музыку, если у меня нет доступа к файловой системе?",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            0);
                }
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(coordinatorLayoutView,
                            "Как, по твоему, я должен открыть музыку, если у меня нет доступа к файловой системе?",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);
                }
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WAKE_LOCK)) {
                    Snackbar.make(coordinatorLayoutView,
                            "Как, по твоему, я должен открыть музыку, если у меня нет доступа к файловой системе?",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WAKE_LOCK}, 0);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WAKE_LOCK},
                            0);
                }
            }

        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }

        if(!file.exists() && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            Log.d("FILE", "FILE");
            file.mkdirs();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .hide(getSupportFragmentManager().findFragmentById(R.id.SearchBox))
                .commit();

        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder bnd) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                binder = (mediaPlayerService.MyBinder) bnd;
                service = binder.getService();
                if (service.mp.isPlaying()) {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction()
                            .show(fm.findFragmentById(R.id.mediaPlayerControlBar))
                            .commit();
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction()
                            .hide(fm.findFragmentById(R.id.mediaPlayerControlBar))
                            .commit();
                }
                bound = true;
                service.setMainActivity(MainActivity.this);
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }

        };

        updatePlaylist();

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(getSupportFragmentManager().findFragmentById(R.id.SearchBox))
                        .commit();
                fab.hide();
                EditText search = (EditText) getSupportFragmentManager().findFragmentById(R.id.SearchBox).getView().findViewById(R.id.searchBox);
                search.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                searchOpened = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(searchOpened) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(getSupportFragmentManager().findFragmentById(R.id.SearchBox))
                    .commit();
            searchOpened = false;
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        intent = new Intent(this, mediaPlayerService.class);
        if (service == null) {
            startService(intent);
        }
        isBound = getApplicationContext().bindService(intent, sConn, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isBound) {
            getApplicationContext().unbindService(sConn);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(track, getApplicationContext().getString(R.string.tracks));
        adapter.addFragment(vktracks, getApplicationContext().getString(R.string.vk));
        adapter.addFragment(deviceTracks, getApplicationContext().getString(R.string.device));
        viewPager.setAdapter(adapter);
    }

    public void updatePlaylist(){
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            sPref = getPreferences(MODE_PRIVATE);
            editor = sPref.edit();
            if (!sPref.getBoolean("ListCreated", false)) {
                final Handler h = new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        ContentResolver cr = getContentResolver();

                        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
                        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
                        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
                        int count;

                        if (cur != null) {
                            count = cur.getCount();

                            if (count > 0) {
                                while (cur.moveToNext()) {
                                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                                    if (data.endsWith(".mp3") || data.endsWith(".flac")) {
                                        File file = new File(data);
                                        mmr.setDataSource(data);
                                        String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                        if (Title == null) {
                                            Title = file.getName();
                                        }
                                        byte Source = 0;
                                        String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                        String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                        if (Album == null) {
                                            Album = "Unknown Album";
                                        }
                                        String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                        if (Artist == null) {
                                            Artist = "Unknown artist";
                                        }
                                        songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, data));
                                    }
                                }

                            }
                        }
                        cur.close();

                        File home_vk = new File(VK_PATH);
                        File[] listOfFilesVK = home_vk.listFiles(new vkmp3Filter());
                        if (listOfFilesVK != null && listOfFilesVK.length > 0) {
                            for (File file : home_vk.listFiles(new vkmp3Filter())) {
                                if(!file.getName().equals("song_storage") && !file.getName().endsWith(".part")) {
                                    mmr.setDataSource(String.valueOf(VK_PATH + file.getName()));
                                    String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                    if (Title == null) {
                                        Title = file.getName();
                                    }
                                    byte Source = 1;
                                    String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                    if (Album == null) {
                                        Album = "Unknown Album";
                                    }
                                    String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                    if (Artist == null) {
                                        Artist = "Unknown Artist";
                                    }
                                    songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, null));
                                }
                            }
                        }

                        File home_uni = new File(UNI_PATH);
                        File[] listOfFilesUNI = home_uni.listFiles(new vkmp3Filter());
                        if (listOfFilesUNI != null && listOfFilesUNI.length > 0) {
                            for (File file : home_uni.listFiles(new vkmp3Filter())) {
                                if(!file.getName().equals("song_storage") && !file.getName().endsWith(".part")) {
                                    mmr.setDataSource(String.valueOf(UNI_PATH + file.getName()));
                                    String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                    if (Title == null) {
                                        Title = file.getName();
                                    }
                                    byte Source = 2;
                                    String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                    if (Album == null) {
                                        Album = "Unknown Album";
                                    }
                                    String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                    if (Artist == null) {
                                        Artist = "Unknown Artist";
                                    }
                                    songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, null));
                                }
                            }
                        }

                        Collections.sort(songs, new Comparator<song>() {
                            @Override
                            public int compare(song lhs, song rhs) {
                                Log.d("LHS", lhs.Title);
                                Log.d("RHS", rhs.Title);
                                Log.d("RES", String.valueOf(lhs.Title.compareTo(rhs.Title)));
                                return lhs.Title.compareTo(rhs.Title);
                            }
                        });
                        editor.putBoolean("ListCreated", true);
                        editor.putString("songs", new Gson().toJson(songs));
                        editor.commit();
                    }
                };

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        h.sendEmptyMessage(0);
                    }
                });
                t.start();
            } else {
                songs = new Gson().fromJson(sPref.getString("songs", ""), new TypeToken<List<song>>() {
                }.getType());
            }
        }
    }

    public void refreshPlaylist(){
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            new refreshTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (service != null) {
            outState.putBoolean("serviceStarted", true);
        } else {
            outState.putBoolean("serviceStarted", false);
        }
        if((getSupportFragmentManager().findFragmentById(R.id.mediaPlayerControlBar)).isVisible()){
            outState.putBoolean("barVisible", true);
        }else{
            outState.putBoolean("barVisible", false);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceStarted = savedInstanceState.getBoolean("mediaControlBarOpened");
        barVisible = savedInstanceState.getBoolean("barVisible");
    }

    public void mediaPlayerStart(int pos, ArrayList<song> songs){
        // Песня из ВКонтакте?
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .show(fm.findFragmentById(R.id.mediaPlayerControlBar))
                .commit();
        service.playerControl(songs, pos);
    }

    public void startFocus(){
        Intent intent = new Intent(this, songFocus.class);
        startActivity(intent);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    class refreshTask extends AsyncTask<String, Void, String>{
        String source;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            songs.clear();
            if (track != null && track.mSwipeRefreshLayout != null) {
                track.mSwipeRefreshLayout.setRefreshing(true);
            }
            if (vktracks != null && vktracks.mSwipeRefreshLayout != null) {
                vktracks.mSwipeRefreshLayout.setRefreshing(true);
            }
            if (deviceTracks != null && deviceTracks.mSwipeRefreshLayout != null) {
                deviceTracks.mSwipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            sPref = getPreferences(MODE_PRIVATE);
            editor = sPref.edit();;
            ContentResolver cr = getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            Cursor cur = cr.query(uri, null, selection, null, sortOrder);
            int count;

            if (cur != null) {
                count = cur.getCount();

                if (count > 0) {
                    while (cur.moveToNext()) {
                        String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                        if (data.endsWith(".mp3") || data.endsWith(".flac")) {
                            File file = new File(data);
                            mmr.setDataSource(data);
                            String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            if (Title == null) {
                                Title = file.getName();
                            }
                            byte Source = 0;
                            String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            if (Album == null) {
                                Album = "Unknown Album";
                            }
                            String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            if (Artist == null) {
                                Artist = "Unknown artist";
                            }
                            songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, data));
                        }
                    }

                }
            }
            cur.close();

            File home_vk = new File(VK_PATH);
            File[] listOfFilesVK = home_vk.listFiles(new vkmp3Filter());
            if (listOfFilesVK != null && listOfFilesVK.length > 0) {
                for (File file : home_vk.listFiles(new vkmp3Filter())) {
                    if(!file.getName().equals("song_storage") && !file.getName().endsWith(".part")) {
                        Log.d("NAME", file.getName());
                        mmr.setDataSource(String.valueOf(VK_PATH + file.getName()));
                        String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        if (Title == null) {
                            Title = file.getName();
                        }
                        byte Source = 1;
                        String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        if (Album == null) {
                            Album = "Unknown Album";
                        }
                        String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        if (Artist == null) {
                            Artist = "Unknown Artist";
                        }
                        songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, null));
                    }
                }
            }

            File home_uni = new File(UNI_PATH);
            File[] listOfFilesUNI = home_uni.listFiles(new vkmp3Filter());
            if (listOfFilesUNI != null && listOfFilesUNI.length > 0) {
                for (File file : home_uni.listFiles(new vkmp3Filter())) {
                    if(!file.getName().equals("song_storage") && !file.getName().endsWith(".part")) {
                        mmr.setDataSource(String.valueOf(UNI_PATH + file.getName()));
                        String Title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        if (Title == null) {
                            Title = file.getName();
                        }
                        byte Source = 2;
                        String Length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String Album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        if (Album == null) {
                            Album = "Unknown Album";
                        }
                        String Artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        if (Artist == null) {
                            Artist = "Unknown Artist";
                        }
                        songs.add(new song(file.getName(), Title, Length, Source, Album, Artist, null));
                    }
                }
            }

            Collections.sort(songs, new Comparator<song>() {
                @Override
                public int compare(song lhs, song rhs) {
                    Log.d("LHS", lhs.Title);
                    Log.d("RHS", rhs.Title);
                    Log.d("RES", String.valueOf(lhs.Title.compareTo(rhs.Title)));
                    return lhs.Title.compareTo(rhs.Title);
                }
            });
            editor.putBoolean("ListCreated", true);
            editor.putString("songs", new Gson().toJson(songs));
            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            Log.d("POST", "POST");
            if(track != null && track.list != null){
                track.refreshList(songs);
            }
            if(vktracks != null && vktracks.list != null){
                vktracks.refreshList(songs);
            }
            if(deviceTracks != null && deviceTracks.list != null){
                deviceTracks.refreshList(songs);
            }

            if (track != null && track.mSwipeRefreshLayout != null) {
                track.mSwipeRefreshLayout.setRefreshing(false);
            }
            if (vktracks != null && vktracks.mSwipeRefreshLayout != null) {
                vktracks.mSwipeRefreshLayout.setRefreshing(false);
            }
            if (deviceTracks != null && deviceTracks.mSwipeRefreshLayout != null) {
                deviceTracks.mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
