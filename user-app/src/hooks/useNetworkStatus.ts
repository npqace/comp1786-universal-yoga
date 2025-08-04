/**
 * @file useNetworkStatus.ts
 * @description A custom hook to detect the network connection status of the device.
 */
import { useState, useEffect } from 'react';
import NetInfo from '@react-native-community/netinfo';

/**
 * @hook useNetworkStatus
 * @description A custom hook that listens to network state changes and returns whether the device is currently offline.
 * @returns {boolean} `true` if the device is offline, `false` otherwise.
 */
export function useNetworkStatus() {
  const [isOffline, setIsOffline] = useState(false);

  useEffect(() => {
    // Subscribe to network state updates
    const unsubscribe = NetInfo.addEventListener(state => {
      setIsOffline(state.isConnected === false);
    });

    // Unsubscribe from the listener when the component unmounts
    return () => {
      unsubscribe();
    };
  }, []);

  return isOffline;
}