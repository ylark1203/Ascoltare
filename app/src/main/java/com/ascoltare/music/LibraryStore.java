package com.ascoltare.music;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LibraryStore {
    private final SharedPreferences preferences;
    private JSONObject data;

    public LibraryStore(Context context) {
        preferences = context.getSharedPreferences("music_library", Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        try {
            data = new JSONObject(preferences.getString("data", "{}"));
        } catch (JSONException e) {
            data = new JSONObject();
        }
    }

    private void save() {
        preferences.edit().putString("data", data.toString()).apply();
    }

    public synchronized boolean isLiked(Track track) {
        if (track == null) return false;
        for (Track item : getLiked()) if (item.getKey().equals(track.getKey())) return true;
        return false;
    }

    public synchronized boolean toggleLike(Track track) {
        JSONArray liked = data.optJSONArray("liked");
        if (liked == null) liked = new JSONArray();
        JSONArray updated = new JSONArray();
        boolean removed = false;
        for (int i = 0; i < liked.length(); i++) {
            JSONObject item = liked.optJSONObject(i);
            if (item != null && track.getKey().equals(Track.fromJson(item).getKey())) removed = true;
            else updated.put(item);
        }
        try {
            if (!removed) updated.put(track.toJson());
            data.put("liked", updated);
            save();
        } catch (JSONException ignored) {
        }
        return !removed;
    }

    public synchronized List<Track> getLiked() { return tracks(data.optJSONArray("liked")); }

    public synchronized Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(String.valueOf(System.currentTimeMillis()), name, new ArrayList<>());
        JSONArray playlists = data.optJSONArray("playlists");
        if (playlists == null) playlists = new JSONArray();
        try {
            JSONObject item = new JSONObject();
            item.put("id", playlist.id);
            item.put("name", playlist.name);
            item.put("coverUri", "");
            item.put("tracks", new JSONArray());
            playlists.put(item);
            data.put("playlists", playlists);
            save();
        } catch (JSONException ignored) {
        }
        return playlist;
    }

    public synchronized void addToPlaylist(String id, Track track) {
        JSONArray playlists = data.optJSONArray("playlists");
        if (playlists == null) return;
        for (int i = 0; i < playlists.length(); i++) {
            JSONObject item = playlists.optJSONObject(i);
            if (item == null || !id.equals(item.optString("id"))) continue;
            JSONArray tracks = item.optJSONArray("tracks");
            if (tracks == null) tracks = new JSONArray();
            for (int j = 0; j < tracks.length(); j++) {
                JSONObject existing = tracks.optJSONObject(j);
                if (existing != null && track.getKey().equals(Track.fromJson(existing).getKey())) return;
            }
            try {
                tracks.put(track.toJson());
                item.put("tracks", tracks);
                save();
            } catch (JSONException ignored) {
            }
            return;
        }
    }

    public synchronized List<Playlist> getPlaylists() {
        List<Playlist> result = new ArrayList<>();
        JSONArray playlists = data.optJSONArray("playlists");
        if (playlists == null) return result;
        for (int i = 0; i < playlists.length(); i++) {
            JSONObject item = playlists.optJSONObject(i);
            if (item != null) result.add(new Playlist(item.optString("id"), item.optString("name"),
                    tracks(item.optJSONArray("tracks")), item.optString("coverUri")));
        }
        return result;
    }

    public synchronized String getPlaylistCover(String id) {
        if ("liked".equals(id)) return data.optString("likedCover");
        for (Playlist playlist : getPlaylists()) if (playlist.id.equals(id)) return playlist.coverUri;
        return "";
    }

    public synchronized void setPlaylistCover(String id, String coverUri) {
        try {
            if ("liked".equals(id)) data.put("likedCover", coverUri);
            else {
                JSONArray playlists = data.optJSONArray("playlists");
                if (playlists == null) return;
                for (int i = 0; i < playlists.length(); i++) {
                    JSONObject item = playlists.optJSONObject(i);
                    if (item != null && id.equals(item.optString("id"))) item.put("coverUri", coverUri);
                }
            }
            save();
        } catch (JSONException ignored) {
        }
    }

    public synchronized void renamePlaylist(String id, String name) {
        JSONArray playlists = data.optJSONArray("playlists");
        if (playlists == null) return;
        for (int i = 0; i < playlists.length(); i++) {
            JSONObject item = playlists.optJSONObject(i);
            if (item == null || !id.equals(item.optString("id"))) continue;
            try {
                item.put("name", name);
                save();
            } catch (JSONException ignored) {
            }
            return;
        }
    }

    private List<Track> tracks(JSONArray array) {
        List<Track> result = new ArrayList<>();
        if (array == null) return result;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) result.add(Track.fromJson(item));
        }
        return result;
    }
}
