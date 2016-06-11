package ua.rodionov.unimusic;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Дмитрий on 24.05.2016.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{
    LayoutInflater lInflater;
    ArrayList<song> objects;
    MainActivity mainActivity;
    Context ctx;

    SongListAdapter(Context context, ArrayList<song> songs, MainActivity _mainActivity) {
        super();
        mainActivity = _mainActivity;
        ctx = context;
        objects = new ArrayList<>(songs);
        lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    public song getItem(int position) {
        return objects.get(position);
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
    public SongListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
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
                mainActivity.mediaPlayerStart(position, objects);
            }
        });
        /*holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle(R.string.contextMenuTitle)
                        .setItems(R.array.contextMenuActions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mainActivity.mediaPlayerStart(position, objects);
                                        break;
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });*/
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
}