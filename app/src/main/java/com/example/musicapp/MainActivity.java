package com.example.musicapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.example.musicapp.Fragments.HomeFragment;
import com.example.musicapp.Fragments.SignInFragment;
import com.example.musicapp.service.MusicService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MusicService musicService;
    private boolean isServiceBound = false;
    private NavController navController;
    private String currentEmail;
    private String currentUsername;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            musicService = null;
        }
    };

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kết nối với MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Thiết lập NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Thiết lập BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Lấy destination hiện tại
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination == null) return false;

            int currentDestinationId = currentDestination.getId();

            // Xử lý điều hướng dựa trên item được chọn
            if (itemId == R.id.nav_home) {
                if (currentDestinationId != R.id.homeFragment) {
                    navController.navigate(R.id.homeFragment);
                }
                return true;
            } else if (itemId == R.id.nav_search) {
                if (currentDestinationId != R.id.searchFragment) {
                    navController.navigate(R.id.searchFragment);
                }
                return true;
            } else if (itemId == R.id.nav_library) {
                if (currentDestinationId != R.id.libraryFragment) {
                    navController.navigate(R.id.libraryFragment);
                }
                return true;
            } else if (itemId == R.id.nav_premium) {
                if (currentDestinationId != R.id.premiumFragment) {
                    navController.navigate(R.id.premiumFragment);
                }
                return true;
            }
            return false;
        });

        // Ẩn BottomNavigationView khi ở màn hình đăng nhập
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            if (destinationId == R.id.signInFragment || destinationId == R.id.signUpFragment ||
                    destinationId == R.id.resetPasswordFragment || destinationId == R.id.resetPasswordFormFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                // Cập nhật trạng thái chọn của BottomNavigationView
                updateBottomNavSelection(destinationId);
            }
        });
    }

    public void gotoHome(){
        Bundle bundle = new Bundle();
        bundle.putString("email", currentEmail);
        bundle.putString("username", currentUsername);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }



    private void updateBottomNavSelection(int destinationId) {
        if (destinationId == R.id.homeFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (destinationId == R.id.searchFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_search);
        } else if (destinationId == R.id.libraryFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_library);
        } else if (destinationId == R.id.premiumFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_premium);
        }
    }
    public void onLoginSuccess(String email,String username) {
        this.currentEmail = email;
        this.currentUsername = username;
        Bundle bundle= new Bundle();
        bundle.putString("email",email);
        bundle.putString("username",username);
        HomeFragment homeFragment= new HomeFragment();
        homeFragment.setArguments(bundle);
        bottomNavigationView.setVisibility(View.VISIBLE);
//        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        navController.navigate(R.id.action_signInFragment_to_homeFragment);
    }

    public String getCurrentEmail() { return currentEmail;}


    public MusicService getMusicService() {
        return musicService;
    }

    public boolean isServiceBound() {
        return isServiceBound;
    }

    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Toast.makeText(this, "Không thể kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

        boolean isConnected = capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));

        if (!isConnected) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        return isConnected;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        if (fragment instanceof SignInFragment) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }
}