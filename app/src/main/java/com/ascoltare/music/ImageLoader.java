package com.ascoltare.music;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(32) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount() / 1024; }
    };

    public static void load(String url, ImageView target) {
        target.setTag(url);
        target.setImageDrawable(null);
        if (url == null || url.isEmpty()) return;
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                Bitmap bitmap;
                if (url.startsWith("content://")) {
                    try (InputStream input = target.getContext().getContentResolver().openInputStream(Uri.parse(url))) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                } else {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(15000);
                    connection.setInstanceFollowRedirects(true);
                    try (InputStream input = connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                }
                if (bitmap == null) return;
                CACHE.put(url, bitmap);
                MAIN.post(() -> {
                    if (url.equals(target.getTag())) target.setImageBitmap(bitmap);
                });
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }
}
