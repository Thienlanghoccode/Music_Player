package com.example.musicapp.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.musicapp.api.DeezerApi;
import com.example.musicapp.entity.Song;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<Map<String, List<Song>>> sectionsLiveData = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> musicSongs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Song>> podcastSongs = new MutableLiveData<>(new ArrayList<>());
    private final Map<String, List<Song>> allSections = new HashMap<>();
    private final DeezerApi deezerApi = new DeezerApi();


    private static class SectionConfig {
        String key;
        String query;
        String artistFilter;

        SectionConfig(String key, String query, String artistFilter) {
            this.key = key;
            this.query = query;
            this.artistFilter = artistFilter;
        }
    }

    public HomeViewModel() {
        fetchSongs();
        fetchMusicSongs();
        fetchPodcastSongs();
    }

    public LiveData<Map<String, List<Song>>> getSections() {
        return sectionsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<List<Song>> getMusicSong() {
        return musicSongs;
    }

    public LiveData<List<Song>> getPodcastSong() {
        return podcastSongs;
    }

    /**
     * Lấy dữ liệu bài hát từ Deezer API cho các danh mục được cấu hình.
     * Cập nhật allSections và sectionsLiveData khi có dữ liệu mới.
     */
    private void fetchSongs() {
        List<SectionConfig> configs = List.of(
                new SectionConfig("my_playlist", "pop vietnam", null),
                new SectionConfig("recently_played", "rock", null),
                new SectionConfig("son_tung_mtp", "artist:\"Son Tung M-TP\"", "Son Tung M-Tp"),
                new SectionConfig("hieuthuhai", "artist:\"HIEUTHUHAI\"", "HIEUTHUHAI"),
                new SectionConfig("featured_songs", "top vietnam", null),
                new SectionConfig("trending_songs", "trending vietnam", null),
                new SectionConfig("top_songs_vietnam", "music", null),
                new SectionConfig("top_50_vietnam", "remix", null),
                new SectionConfig("top_50_global", "music", null),
                new SectionConfig("top_songs_global", "edm", null)
        );



        for (SectionConfig config : configs) {
            allSections.put(config.key, new ArrayList<>());
            deezerApi.searchTracks(config.query, songs -> {
                if (songs == null || songs.isEmpty()) {
                    errorLiveData.postValue("Không tìm thấy bài hát cho: " + config.query);
                    Log.e(TAG, "Không có bài hát trả về cho truy vấn: " + config.query);
                    return;
                }

                List<Song> filteredSongs = filterSongs(songs, config.artistFilter);
                allSections.put(config.key, filteredSongs);

                Map<String, List<Song>> updatedSections = new HashMap<>(allSections);
                updatedSections.put(config.key, limitSongs(filteredSongs, 8));
                sectionsLiveData.postValue(updatedSections);

                Log.d(TAG, "Danh mục: " + config.key + ", Tổng số bài hát: " + filteredSongs.size() + ", Hiển thị: " + Objects.requireNonNull(updatedSections.get(config.key)).size());
            });
        }
    }


    /**
     * Lấy danh sách bài hát cho tab Music.
     */
    private void fetchMusicSongs() {
        deezerApi.searchTracks("music", songs -> {
            if (songs == null || songs.isEmpty()) {
                musicSongs.postValue(new ArrayList<>());
                errorLiveData.postValue("Không tìm thấy bài hát cho tab Music");
                Log.e(TAG, "Không tìm thấy bài hát cho tab Music");
                return;
            }

            List<Song> shuffleSong = new ArrayList<>(songs);
            Collections.shuffle(shuffleSong);
            int limit = Math.min(shuffleSong.size(), 30);
            musicSongs.postValue(new ArrayList<>(shuffleSong.subList(0, limit)));
            Log.d(TAG, "Tải thành công " + limit + " bài hát cho tab Music");
        });
    }

    /**
     * Lấy danh sách bài hát cho tab Podcasts.
     */
    private void fetchPodcastSongs() {
        deezerApi.searchTracks("podcast", songs -> {
            if (songs == null || songs.isEmpty()) {
                podcastSongs.postValue(new ArrayList<>());
                errorLiveData.postValue("Không tìm thấy podcast");
                Log.e(TAG, "Không tìm thấy podcast");
                return;
            }

            List<Song> shuffleSong = new ArrayList<>(songs);
            Collections.shuffle(shuffleSong);
            int limit = Math.min(shuffleSong.size(), 20);
            podcastSongs.postValue(new ArrayList<>(shuffleSong.subList(0, limit)));
            Log.d(TAG, "Tải thành công " + limit + " podcast");
        });
    }


    public List<Song> getSongsBySection(String sectionKey) {
        return limitSongs(Objects.requireNonNull(allSections.getOrDefault(sectionKey, new ArrayList<>())), 15);
    }

    /**
     * Lọc bài hát để loại bỏ trùng lặp và áp dụng bộ lọc nghệ sĩ nếu có.
     * @param songs Danh sách bài hát gốc.
     * @param artistFilter Tên nghệ sĩ để lọc (null nếu không lọc).
     * @return Danh sách bài hát đã lọc.
     */
    private List<Song> filterSongs(List<Song> songs, String artistFilter) {
        Set<String> songIdentifiers = new HashSet<>();
        List<Song> uniqueSongs = songs.stream()
                .filter(song -> songIdentifiers.add(song.getTitle() + " - " + song.getArtist()))
                .collect(Collectors.toList());

        return artistFilter != null
                ? uniqueSongs.stream()
                .filter(song -> song.getArtist().toLowerCase().contains(artistFilter.toLowerCase()))
                .collect(Collectors.toList())
                : uniqueSongs;
    }

    private List<Song> limitSongs(List<Song> songs, int max) {
        return songs.size() > max ? new ArrayList<>(songs.subList(0, max)) : songs;
    }
}