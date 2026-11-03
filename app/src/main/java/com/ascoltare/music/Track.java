package com.ascoltare.music;

import org.json.JSONException;
import org.json.JSONObject;

public class Track {
    public final String query;
    public final int index;
    public final String title;
    public final String artist;
    public final String album;
    public final String coverUrl;
    public final String audioUrl;
    public final String lyrics;

    public Track(String query, int index, String title, String artist, String album, String coverUrl, String audioUrl) {
        this(query, index, title, artist, album, coverUrl, audioUrl, "");
    }

    public Track(String query, int index, String title, String artist, String album, String coverUrl, String audioUrl, String lyrics) {
        this.query = query;
        this.index = index;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.audioUrl = audioUrl;
        this.lyrics = lyrics;
    }

    public String getKey() { return query + "#" + index; }

    public Track withDetails(JSONObject data) {
        String artistName = artist;
        Object artists = data.opt("artists");
        if (artists instanceof org.json.JSONArray) {
            org.json.JSONArray array = (org.json.JSONArray) artists;
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                if (names.length() > 0) names.append(" / ");
                names.append(item.optString("name"));
            }
            if (names.length() > 0) artistName = names.toString();
        } else if (artists != null) {
            artistName = String.valueOf(artists);
        }

        String albumName = album;
        Object albumData = data.opt("album");
        if (albumData instanceof JSONObject) albumName = ((JSONObject) albumData).optString("name", album);
        else if (albumData != null) albumName = String.valueOf(albumData);

        String imageUrl = coverUrl;
        Object cover = data.opt("cover");
        if (cover instanceof JSONObject) imageUrl = ((JSONObject) cover).optString("large", coverUrl);
        else if (cover != null) imageUrl = String.valueOf(cover);
        String lrc = "";
        JSONObject lyric = data.optJSONObject("lyric");
        if (lyric != null) lrc = lyric.optString("text");
        return new Track(query, index, data.optString("name", title), artistName, albumName, imageUrl,
                data.optString("url"), lrc);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("query", query);
        json.put("index", index);
        json.put("title", title);
        json.put("artist", artist);
        json.put("album", album);
        json.put("coverUrl", coverUrl);
        json.put("audioUrl", audioUrl);
        json.put("lyrics", lyrics);
        return json;
    }

    public static Track fromJson(JSONObject json) {
        return new Track(json.optString("query"), json.optInt("index"), json.optString("title"),
                json.optString("artist"), json.optString("album"), json.optString("coverUrl"),
                json.optString("audioUrl"), json.optString("lyrics"));
    }
}
