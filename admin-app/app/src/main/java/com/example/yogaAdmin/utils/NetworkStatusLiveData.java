package com.example.yogaAdmin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * A {@link LiveData} subclass that observes network connectivity status.
 * It provides real-time updates on whether the device has an active internet connection.
 * This allows UI components to reactively show or hide online/offline indicators.
 */
public class NetworkStatusLiveData extends LiveData<Boolean> {

    // System service for managing network connectivity.
    private final ConnectivityManager connectivityManager;
    // Callback for network events.
    private final ConnectivityManager.NetworkCallback networkCallback;

    /**
     * Constructor for NetworkStatusLiveData.
     *
     * @param context The application context, used to get the ConnectivityManager service.
     */
    public NetworkStatusLiveData(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Initialize the network callback to handle network state changes.
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // A network is available.
                updateNetworkStatus();
            }

            @Override
            public void onLost(@NonNull Network network) {
                // A network was lost.
                updateNetworkStatus();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                // The capabilities of a network have changed (e.g., gained/lost internet access).
                updateNetworkStatus();
            }
        };
    }

    /**
     * Called when the LiveData becomes active (i.e., has at least one observer).
     * Registers the network callback to start listening for connectivity changes.
     */
    @Override
    protected void onActive() {
        super.onActive();
        // Post the initial network status.
        updateNetworkStatus();
        // Register the callback to receive network updates.
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    /**
     * Called when the LiveData becomes inactive (i.e., has no observers).
     * Unregisters the network callback to stop listening and save resources.
     */
    @Override
    protected void onInactive() {
        super.onInactive();
        // Unregister the callback to stop receiving updates.
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    /**
     * Checks the current network capabilities and posts the connection status to the LiveData.
     */
    private void updateNetworkStatus() {
        // Get the capabilities of the active network.
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        // Check if the network has the capability to access the internet.
        boolean isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Post the boolean value to observers.
        postValue(isConnected);
    }
}
