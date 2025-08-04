/**
 * @file ErrorMessage.tsx
 * @description A component to display an error message with an optional retry button.
 */
import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, globalStyles, spacing, typography } from '../styles/globalStyles';

/**
 * @interface ErrorMessageProps
 * @description Props for the ErrorMessage component.
 * @property {string} message - The error message to display.
 * @property {() => void} [onRetry] - An optional function to call when the retry button is pressed.
 * @property {string} [retryText] - The text for the retry button. Defaults to "Try Again".
 */
interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  retryText?: string;
}

/**
 * @component ErrorMessage
 * @description A reusable component to display a standardized error message to the user, with an option to retry an action.
 * @param {ErrorMessageProps} props - The props for the component.
 */
export default function ErrorMessage({ message, onRetry, retryText = "Try Again" }: ErrorMessageProps) {
  return (
    <View style={styles.container}>
      <Ionicons name="cloud-offline-outline" size={80} color={colors.error} />
      <Text style={styles.title}>An Error Occurred</Text>
      <Text style={styles.message}>{message}</Text>
      {/* Conditionally render the retry button if an onRetry function is provided */}
      {onRetry && (
        <TouchableOpacity style={styles.retryButton} onPress={onRetry}>
          <Text style={styles.retryButtonText}>{retryText}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    ...globalStyles.emptyStateContainer,
  },
  title: {
    ...typography.h2,
    color: colors.text,
    textAlign: 'center',
    marginTop: spacing.lg,
    marginBottom: spacing.sm,
  },
  message: {
    ...typography.body,
    color: colors.textLight,
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: spacing.lg,
  },
  retryButton: {
    ...globalStyles.button,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
  },
  retryButtonText: {
    ...globalStyles.buttonText,
  },
});