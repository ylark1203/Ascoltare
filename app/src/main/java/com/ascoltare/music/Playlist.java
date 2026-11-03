package com.ascoltare.music;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    public final String id;
    public final String name;
    public final List<Track> tracks;
    public final String coverUri;

    public Playlist(String id, String name, List<Track> tracks) {
        this(id, name, tracks, "");
    }

    public Playlist(String id, String name, List<Track> tracks, String coverUri) {
        this.id = id;
        this.name = name;
        this.tracks = new ArrayList<>(tracks);
        this.coverUri = coverUri;
    }
}
