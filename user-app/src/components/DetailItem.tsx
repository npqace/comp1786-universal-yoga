/**
 * @file DetailItem.tsx
 * @description A component to display a single detail item with an icon, label, and value.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, typography } from '../styles/globalStyles';

/**
 * @interface DetailItemProps
 * @description Props for the DetailItem component.
 * @property {keyof typeof Ionicons.glyphMap} icon - The name of the icon to display.
 * @property {string} label - The label for the detail item.
 * @property {string | number | undefined | null} value - The value of the detail item.
 * @property {string} [unit] - An optional unit to append to the value (e.g., "minutes", "people").
 */
interface DetailItemProps {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  value: string | number | undefined | null;
  unit?: string;
}

/**
 * @component DetailItem
 * @description A reusable component to display a piece of information with an icon, label, and value.
 * @param {DetailItemProps} props - The props for the component.
 */
const DetailItem: React.FC<DetailItemProps> = ({ icon, label, value, unit }) => {
  return (
    <View style={styles.detailRow}>
      <Ionicons name={icon} size={20} color={colors.primary} />
      <View style={styles.detailContent}>
        <Text style={styles.detailLabel}>{label}</Text>
        <Text style={styles.detailValue}>
          {/* Display the value and unit, ensuring the unit only shows if there's a value */}
          {value || ''} {value ? unit : ''}
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  detailContent: {
    marginLeft: spacing.md,
    flex: 1,
  },
  detailLabel: {
    ...typography.caption,
    color: colors.textLight,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  detailValue: {
    ...typography.body,
    color: colors.text,
    marginTop: spacing.xs,
  },
});

export default DetailItem;