package ua.rodionov.unimusic;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Дмитрий on 05.06.2016.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder>{

    Context ctx;
    ArrayList<VKSong> objects = new ArrayList<>();
    LayoutInflater lInflater;
    MainActivity mainActivity;

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

        public ViewHolder(View v) {
            super(v);
            txtName = (TextView) v.findViewById(R.id.songName);
            txtArtist = (TextView) v.findViewById(R.id.artistName);
            songCover = (ImageView) v.findViewById(R.id.songCover);
            layout = (RelativeLayout) v.findViewById(R.id.layout);
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
                mainActivity.service.startPreviewPlayer(objects.get(position).getURL());
            }
        });
    }
}
