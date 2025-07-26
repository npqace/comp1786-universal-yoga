package com.example.yogaAdmin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkStatusLiveData extends LiveData<Boolean> {

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkStatusLiveData(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                updateNetworkStatus();
            }

            @Override
            public void onLost(@NonNull Network network) {
                updateNetworkStatus();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                updateNetworkStatus();
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateNetworkStatus();
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private void updateNetworkStatus() {
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        boolean isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        postValue(isConnected);
    }
}
