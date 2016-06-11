package ua.rodionov.unimusic;

/**
 * Created by Дмитрий on 05.06.2016.
 */
public class VKSong {
    long id;
    String Title;
    String URL;
    String Artist;
    VKSong(long _id, String _title, String _Artist, String _URL){
        id = _id;
        Title = _title;
        Artist = _Artist;
        URL = _URL;
    }

    public String getArtist(){
        return Artist;
    }
    public String getTitle(){
        return Title;
    }
    public String getURL(){
        return URL;
    }
}