/**
 * @file OfflineBanner.tsx
 * @description A banner that appears at the top of the screen when the user is offline.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNetworkStatus } from '../hooks/useNetworkStatus';
import { colors, spacing, typography } from '../styles/globalStyles';
import { Ionicons } from '@expo/vector-icons';

/**
 * @component OfflineBanner
 * @description A banner that is displayed at the top of the screen when the device is not connected to the internet.
 * It uses the `useNetworkStatus` hook to detect the connection state.
 */
export default function OfflineBanner() {
  const isOffline = useNetworkStatus();

  // The banner is only rendered if the device is offline
  if (!isOffline) {
    return null;
  }

  return (
    <View style={[styles.banner]}>
      <Ionicons name="cloud-offline-outline" size={20} color="white" />
      <Text style={styles.bannerText}>You are currently offline</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    backgroundColor: colors.error,
    paddingVertical: spacing.sm,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  bannerText: {
    color: 'white',
    marginLeft: spacing.sm,
    ...typography.body,
    fontWeight: 'bold',
  },
});