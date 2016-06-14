package ua.rodionov.unimusic;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Дмитрий on 05.06.2016.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder>{

    Context ctx;
    ArrayList<VKSong> objects = new ArrayList<>();
    LayoutInflater lInflater;
    MainActivity mainActivity;
    public CircularProgressBar pb;
    public ImageButton db;
    private static final String UNI_PATH = Environment.getExternalStorageDirectory().getPath() + "/.UniMusic/audio/";
    File file = new File(UNI_PATH + "song_storage");

    SearchResultAdapter(Context context, ArrayList<VKSong> songs, MainActivity _mainActivity){
        super();
        ctx = context;
        objects = songs;
        mainActivity = _mainActivity;
        lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtArtist;
        public ImageView songCover;
        public RelativeLayout layout;
        public ImageButton downloadButton;
        public CircularProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);
            txtName = (TextView) v.findViewById(R.id.songName);
            txtArtist = (TextView) v.findViewById(R.id.artistName);
            songCover = (ImageView) v.findViewById(R.id.songCover);
            layout = (RelativeLayout) v.findViewById(R.id.layout);
            downloadButton = (ImageButton) v.findViewById(R.id.downloadButton);
            progressBar = (CircularProgressBar) v.findViewById(R.id.ProgressBar);
        }
    }

    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_song_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.txtName.setText(objects.get(position).getTitle());
        holder.txtArtist.setText(objects.get(position).getArtist());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.service.startPreviewPlayer(objects.get(position).getURL(),objects.get(position).getTitle());
            }
        });
        holder.downloadButton.setTag(holder);
        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.WAKE_LOCK)
                        == PackageManager.PERMISSION_GRANTED) {
                    pb = ((ViewHolder) v.getTag()).progressBar;
                    db = (ImageButton) v;
                    new DownloadTask(ctx).execute(objects.get(position).getTitle(), objects.get(position).getArtist(),
                            objects.get(position).getId(), objects.get(position).getURL());
                }else{
                    Toast.makeText(ctx,"No permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(file.exists()) {

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.equals(objects.get(position).getId())) {
                        holder.downloadButton.setOnClickListener(null);
                        holder.downloadButton.setImageResource(R.drawable.ic_done_black_36dp);

                    }
                }
                br.close();
            } catch (IOException e) {
                //
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        String filename = String.valueOf(System.currentTimeMillis());
        String id,artist,title;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            pb.setVisibility(View.VISIBLE);
            db.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            pb.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            title =  params[0];
            artist = params[1];
            id = params[2];
            try {
                URL url = new URL(params[3]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(UNI_PATH + filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            pb.setVisibility(View.GONE);
            db.setVisibility(View.VISIBLE);
            db.setImageResource(R.drawable.ic_done_black_36dp);

            try {
                ID3v2 id3v2Tag;
                Mp3File mp3file = new Mp3File(UNI_PATH+filename);
                if (mp3file.hasId3v1Tag()) {
                    mp3file.removeId3v1Tag();
                }
                if (mp3file.hasId3v2Tag()) {
                    mp3file.removeId3v2Tag();
                }
                if (mp3file.hasCustomTag()) {
                    mp3file.removeCustomTag();
                }
                id3v2Tag = new ID3v24Tag();
                mp3file.setId3v2Tag(id3v2Tag);
                id3v2Tag.setArtist(artist);
                id3v2Tag.setTitle(title);
                id3v2Tag.setComment(id);
                mp3file.save(UNI_PATH+filename+"_tagged");
                File fl = new File(UNI_PATH+filename);
                fl.delete();
            }catch(Exception e){
                e.printStackTrace();
            }

            try {

                if (!file.exists()) {
                    file.createNewFile();
                }

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                ArrayList<String> temp = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    temp.add(line);
                }
                br.close();

                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                for (String s : temp) {
                    myOutWriter.append(s);
                    myOutWriter.append('\n');
                }

                myOutWriter.append(id);
                myOutWriter.append('\n');

                myOutWriter.close();
                fOut.close();
            }catch (IOException e){
                //
            }

            mainActivity.refreshPlaylist();

            //TODO: previewPlayer.

            if (result != null)
                Toast.makeText(context,ctx.getString(R.string.download_error)+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,ctx.getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
    }
    }
}
