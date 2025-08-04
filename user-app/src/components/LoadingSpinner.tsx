/**
 * @file LoadingSpinner.tsx
 * @description A component to display a loading indicator.
 */
import React from 'react';
import { View, ActivityIndicator, Text } from 'react-native';
import { globalStyles, colors } from '../styles/globalStyles';

/**
 * @interface LoadingSpinnerProps
 * @description Props for the LoadingSpinner component.
 * @property {string} [message] - An optional message to display below the spinner.
 * @property {'small' | 'large'} [size] - The size of the activity indicator.
 */
interface LoadingSpinnerProps {
  message?: string;
  size?: 'small' | 'large';
}

/**
 * @component LoadingSpinner
 * @description A reusable component to indicate a loading state, with an optional message.
 * @param {LoadingSpinnerProps} props - The props for the component.
 */
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