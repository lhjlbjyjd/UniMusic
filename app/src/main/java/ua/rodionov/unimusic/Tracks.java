package ua.rodionov.unimusic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

class vkmp3Filter implements FilenameFilter{
    public boolean accept(File dir, String name){
        return !name.endsWith(".covers");
    }
}


public class Tracks extends Fragment {


    RecyclerView list;
    SongListAdapter listAdapter;
    public ArrayList<song> songs = new ArrayList<>();
    MainActivity mainActivity;


    public Tracks() {
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
        songs = mainActivity.songs;
        listAdapter = new SongListAdapter(getContext(),songs, mainActivity);
        listAdapter.notifyDataSetChanged();
        list.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        list.setLayoutManager(llm);
        list.setAdapter(listAdapter);
    }

    public void refreshList(ArrayList<song> _songs){
        songs = _songs;
        listAdapter = new SongListAdapter(getContext(),songs, mainActivity);
        list.swapAdapter(listAdapter, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tracks, container, false);

        list = (RecyclerView) view.findViewById(R.id.list);
        return view;
    }
}