package ua.rodionov.unimusic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class mediaPlayerControlBar extends Fragment {

    View view;
    public CircularProgressBar progressBar;
    MainActivity mainActivity;
    ImageButton playButton;


    @Override
    public void onResume (){
        super.onResume();
        mainActivity = (MainActivity)getActivity();
        if(this.getView() == null){
            Log.d("Noview", "1");
        }
        //mainActivity.setFragment(this);
        view.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mainActivity.startFocus();
            }
        });
        playButton = (ImageButton) view.findViewById(R.id.playButton);
        playButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!mainActivity.service.playerActive)
                {
                    mainActivity.service.pausePlayer();
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                }else{
                    mainActivity.service.resumePlayer();
                    playButton.setImageResource(R.drawable.ic_pause_black_48dp);
                }
            }

        });
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("CREATEVIEW", "CREATEWIEW");
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.media_control, container, false);
        this.progressBar = (CircularProgressBar) view.findViewById(R.id.ProgressBar);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("DETACH", "DETACH");
    }

    public void setProgressBar(float value){
        progressBar.setProgress(value);
    }

}
