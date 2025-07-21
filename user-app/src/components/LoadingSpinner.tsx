import React from 'react';
import { View, ActivityIndicator, Text } from 'react-native';
import { globalStyles, colors } from '../styles/globalStyles';

interface LoadingSpinnerProps {
  message?: string;
  size?: 'small' | 'large';
}

export default function LoadingSpinner({ message = 'Loading...', size = 'large' }: LoadingSpinnerProps) {
  return (
    <View style={globalStyles.loadingContainer}>
      <ActivityIndicator size={size} color={colors.primary} />
      {message && (
        <Text style={[globalStyles.emptyStateText, { marginTop: 16 }]}>
          {message}
        </Text>
      )}
    </View>
  );
} 