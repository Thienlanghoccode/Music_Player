package com.example.musicapp.api;

import android.util.Log;
import androidx.collection.LruCache;
import com.example.musicapp.entity.Song;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeezerApi {

    private static final String TAG = "DeezerApi";
    private static final String BASE_URL = "https://api.deezer.com";
    private static final int CACHE_SIZE = 100;
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final LruCache<String, List<Song>> searchCache = new LruCache<>(CACHE_SIZE);

    public void searchTracks(String query, Consumer<List<Song>> callback) {
        String cacheKey = query.toLowerCase().trim();
        List<Song> cachedResult = searchCache.get(cacheKey);
        if (cachedResult != null) {
            callback.accept(cachedResult);
            Log.d(TAG, "Trả về kết quả từ cache cho truy vấn: " + query);
            return;
        }

        String url = BASE_URL + "/search?q=" + query.replace(" ", "%20");
        Request request = new Request.Builder().url(url).build();

        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    callback.accept(null);
                    return;
                }

                assert response.body() != null;
                String json = response.body().string();
                List<Song> songs = parseSearchResults(json);
                searchCache.put(cacheKey, songs);
                callback.accept(songs);
            } catch (IOException e) {
                Log.e(TAG, "Lỗi mạng: " + e.getMessage(), e);
                callback.accept(null);
            }
        });
    }

    public void getTrack(String trackId, Consumer<Song> callback) {
        String url = BASE_URL + "/track/" + trackId;
        Request request = new Request.Builder().url(url).build();

        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    callback.accept(null);
                    return;
                }

                assert response.body() != null;
                String json = response.body().string();
                Song song = parseTrack(json);
                callback.accept(song);
            } catch (IOException e) {
                Log.e(TAG, "Lỗi mạng: " + e.getMessage(), e);
                callback.accept(null);
            }
        });
    }

    private List<Song> parseSearchResults(String json) {
        List<Song> results = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0; i < Math.min(data.length(), 20); i++) {
                JSONObject track = data.getJSONObject(i);
                String id = track.getString("id");
                String title = track.getString("title");
                String previewUrl = track.getString("preview");
                JSONObject artist = track.getJSONObject("artist");
                String artistName = artist.getString("name");
                String imageUrl = track.getJSONObject("album").getString("cover_medium");
                results.add(new Song(id, title, artistName, previewUrl, imageUrl));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi phân tích JSON: " + e.getMessage());
        }
        return results;
    }

    private Song parseTrack(String json) {
        try {
            JSONObject track = new JSONObject(json);
            String id = track.getString("id");
            String title = track.getString("title");
            String previewUrl = track.getString("preview");
            JSONObject artist = track.getJSONObject("artist");
            String artistName = artist.getString("name");
            String imageUrl = track.getJSONObject("album").getString("cover_medium");
            return new Song(id, title, artistName, previewUrl, imageUrl);
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi phân tích JSON: " + e.getMessage());
            return null;
        }
    }
}