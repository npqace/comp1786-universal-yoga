/**
 * @file EmptyState.tsx
 * @description A component to display when there is no data to show.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, globalStyles, spacing, typography } from '../styles/globalStyles';

/**
 * @interface EmptyStateProps
 * @description Props for the EmptyState component.
 * @property {keyof typeof Ionicons.glyphMap} icon - The name of the icon to display.
 * @property {string} title - The main title of the empty state message.
 * @property {string} message - The detailed message to display.
 */
interface EmptyStateProps {
  icon: keyof typeof Ionicons.glyphMap;
  title: string;
  message: string;
}

/**
 * @component EmptyState
 * @description A reusable component to display a message when a list is empty or there is no data.
 * @param {EmptyStateProps} props - The props for the component.
 */
export default function EmptyState({ icon, title, message }: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <Ionicons name={icon} size={80} color={colors.textLight} />
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.message}>{message}</Text>
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
  },
});