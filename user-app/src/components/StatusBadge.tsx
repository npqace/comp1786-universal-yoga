/**
 * @file StatusBadge.tsx
 * @description A component to display a status badge with different colors based on the status.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors, spacing, borderRadius, typography } from '../styles/globalStyles';

/**
 * @type Status
 * @description Defines the possible status types for the badge.
 */
export type Status = 'scheduled' | 'completed' | 'cancelled' | 'active';

/**
 * @interface StatusBadgeProps
 * @description Props for the StatusBadge component.
 * @property {string} [status] - The status to display.
 */
interface StatusBadgeProps {
  status?: string;
}

/**
 * @function getStatusBackgroundColor
 * @description Determines the background color of the badge based on the status.
 * @param {Status} [status] - The status.
 * @returns {string} The corresponding background color.
 */
const getStatusBackgroundColor = (status?: Status): string => {
  switch (status) {
    case 'active':
      return colors.primary;
    case 'completed':
      return colors.success;
    case 'cancelled':
      return colors.error;
    default:
      return colors.primary; // Default for 'scheduled' or undefined
  }
};

/**
 * @component StatusBadge
 * @description A small badge component to visually represent the status of an item (e.g., a yoga class).
 * @param {StatusBadgeProps} props - The props for the component.
 */
const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  // Normalize status to lowercase to handle potential inconsistencies in data
  const normalizedStatus = status?.toLowerCase() as Status;
  const backgroundColor = getStatusBackgroundColor(normalizedStatus);

  return (
    <View style={[styles.statusContainer, { backgroundColor }]}>
      <Text style={styles.statusText}>
        {status?.toUpperCase() || 'SCHEDULED'}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  statusContainer: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: borderRadius.small,
    alignSelf: 'flex-start',
  },
  statusText: {
    ...typography.caption,
    color: colors.surface,
    fontWeight: 'bold',
  },
});

export default StatusBadge;