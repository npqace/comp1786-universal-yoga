import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors, spacing, borderRadius, typography } from '../styles/globalStyles';

export type Status = 'scheduled' | 'completed' | 'cancelled';

interface StatusBadgeProps {
  status?: string;
}

const getStatusBackgroundColor = (status?: Status): string => {
  switch (status) {
    case 'completed':
      return colors.success;
    case 'cancelled':
      return colors.warning;
    case 'scheduled':
    default:
      return colors.primary;
  }
};

const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
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