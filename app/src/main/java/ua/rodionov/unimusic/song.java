package ua.rodionov.unimusic;

public class song{
        String Name;
        String Title;
        String Length;
        byte Source;
        String Album;
        String Artist;
        String data;
        song(String _name, String _title, String _length, byte _source, String _album, String _artist, String _data){
            Name = _name.trim();
            Title = _title.trim();
            Length = _length.trim();
            Source = _source;
            Album = _album.trim();
            Artist = _artist.trim();
            data = _data;
        }
    public String getName(){
        return Name;
    }
    public String getArtist(){
        return Artist;
    }
    public String getTitle(){
        return Title;
    }
    public byte getSource(){
        return Source;
    }
    public String getDuration(){
        return Length;
    }
}
