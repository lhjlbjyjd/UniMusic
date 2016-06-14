package ua.rodionov.unimusic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Дмитрий on 05.06.2016.
 */
public class SearchBox extends Fragment {

    View view;
    EditText searchBox;
    ArrayList<VKSong> VKSongs = new ArrayList<>();
    RecyclerView list;
    SearchResultAdapter listAdapter;
    MainActivity mainActivity;
    boolean adapterSet = false;
    ImageButton backButton;

    public void onResume (){
        super.onResume();
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("CREATEVIEW", "CREATEWIEW");
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.search_box, container, false);
        mainActivity = (MainActivity) getActivity();
        backButton = (ImageButton) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.searchOpened = false;
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .hide(mainActivity.getSupportFragmentManager().findFragmentById(R.id.SearchBox))
                        .commit();
                InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                mainActivity.fab.show();
            }
        });
        list = (RecyclerView) view.findViewById(R.id.SearchResultList);
        searchBox = (EditText) view.findViewById(R.id.searchBox);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(String.valueOf(v.getText()));
                    handled = true;
                }
                return handled;
            }
        });
        return view;
    }

    private void performSearch(String searchQuery){
        VKSongs.clear();
        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.Q, searchQuery);
        vkParameters.put(VKApiConst.COUNT, 30);
        VKRequest request = VKApi.audio().search(vkParameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(final VKResponse response) {
                Log.d("VK", response.json.toString());
                final Handler h = new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        try {
                            JSONObject responseJson = response.json.getJSONObject("response");
                            JSONArray items = responseJson.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                VKSongs.add(new VKSong(obj.getLong("id"), obj.getString("title"), obj.getString("artist"), obj.getString("url")));
                            }
                            if(!adapterSet) {
                                listAdapter = new SearchResultAdapter(getContext(), VKSongs, mainActivity);
                                listAdapter.notifyDataSetChanged();
                                list.setHasFixedSize(true);
                                LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                                list.setLayoutManager(llm);
                                list.setAdapter(listAdapter);
                            }else{
                                listAdapter = new SearchResultAdapter(getContext(), VKSongs, mainActivity);
                                listAdapter.notifyDataSetChanged();
                                list.swapAdapter(listAdapter, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        h.sendEmptyMessage(0);
                    }
                });
                t.start();
            }
        });
    }
}
