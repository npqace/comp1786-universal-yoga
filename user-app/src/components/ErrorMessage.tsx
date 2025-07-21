import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, globalStyles, spacing, typography } from '../styles/globalStyles';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  retryText?: string;
}

export default function ErrorMessage({ message, onRetry, retryText = "Try Again" }: ErrorMessageProps) {
  return (
    <View style={styles.container}>
      <Ionicons name="cloud-offline-outline" size={80} color={colors.error} />
      <Text style={styles.title}>An Error Occurred</Text>
      <Text style={styles.message}>{message}</Text>
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