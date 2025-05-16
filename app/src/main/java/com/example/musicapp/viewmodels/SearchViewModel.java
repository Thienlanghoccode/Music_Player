package com.example.musicapp.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.musicapp.api.DeezerApi;
import com.example.musicapp.entity.Song;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý logic tìm kiếm bài hát qua Deezer API.
 * Cung cấp kết quả tìm kiếm và thông báo lỗi qua LiveData.
 */
public class SearchViewModel extends ViewModel {
    private static final String TAG = "SearchViewModel";
    private final DeezerApi deezerApi = new DeezerApi();
    private final MutableLiveData<List<Song>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private String lastQuery;


    public LiveData<List<Song>> getSearchResults() {
        return searchResults;
    }


    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Thực hiện tìm kiếm bài hát dựa trên truy vấn.
     * @param query Từ khóa tìm kiếm (tên bài hát, nghệ sĩ).
     */
    public void searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.postValue(new ArrayList<>());
            errorMessage.postValue("Vui lòng nhập từ khóa tìm kiếm");
            Log.w(TAG, "Truy vấn rỗng");
            return;
        }

        lastQuery = query.trim();
        deezerApi.searchTracks(lastQuery, songs -> {
            if (songs == null) {
                searchResults.postValue(new ArrayList<>());
                errorMessage.postValue("Lỗi kết nối mạng hoặc dữ liệu không hợp lệ");
                Log.e(TAG, "API trả về null cho truy vấn: " + lastQuery);
            } else if (songs.isEmpty()) {
                searchResults.postValue(new ArrayList<>());
                errorMessage.postValue("Không tìm thấy bài hát cho: " + lastQuery);
                Log.w(TAG, "Không có kết quả cho truy vấn: " + lastQuery);
            } else {
                searchResults.postValue(new ArrayList<>(songs));
                errorMessage.postValue(null);
                Log.d(TAG, "Tìm thấy " + songs.size() + " bài hát cho: " + lastQuery);
            }
        });
    }

    public String getLastQuery() {
        return lastQuery;
    }
}
