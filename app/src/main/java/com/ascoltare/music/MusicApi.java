package com.ascoltare.music;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicApi {
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private static final String ENDPOINT = "https://cyapi.top/API/qq_music.php";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public void search(String query, Callback<List<Track>> callback) {
        executor.execute(() -> {
            try {
                JSONArray songs = request(query, 0).getJSONArray("list");
                List<Track> tracks = new ArrayList<>();
                for (int i = 0; i < songs.length(); i++) {
                    JSONObject song = songs.getJSONObject(i);
                    tracks.add(new Track(query, i + 1, song.optString("name"),
                            song.optString("artists"), "", song.optString("cover"), ""));
                }
                success(callback, tracks);
            } catch (Exception e) {
                error(callback, e.getMessage());
            }
        });
    }

    public void getDetails(Track track, Callback<Track> callback) {
        executor.execute(() -> {
            try {
                Track detailed = track.withDetails(request(track.query, track.index));
                if (detailed.audioUrl.isEmpty()) throw new IOException("接口未返回播放地址");
                success(callback, detailed);
            } catch (Exception e) {
                error(callback, e.getMessage());
            }
        });
    }

    private JSONObject request(String query, int index) throws Exception {
        StringBuilder url = new StringBuilder(ENDPOINT);
        url.append("?apikey=").append(URLEncoder.encode(BuildConfig.MUSIC_API_KEY, "UTF-8"));
        url.append("&msg=").append(URLEncoder.encode(query, "UTF-8"));
        url.append("&num=50&type=json");
        if (index > 0) url.append("&n=").append(index);
        HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Ascoltare/2.1 Android");
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String body = read(stream);
            JSONObject response = new JSONObject(body);
            if (status != 200 || response.has("code") && response.optInt("code") != 200) {
                throw new IOException(response.optString("msg", "请求失败（HTTP " + status + "）"));
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String read(InputStream stream) throws IOException {
        if (stream == null) throw new IOException("服务器未返回内容");
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private <T> void success(Callback<T> callback, T value) {
        main.post(() -> callback.onSuccess(value));
    }

    private <T> void error(Callback<T> callback, String message) {
        main.post(() -> callback.onError(message == null ? "网络请求失败" : message));
    }

    public void shutdown() { executor.shutdownNow(); }
}
