/**
 * @file DetailHeader.tsx
 * @description A header component for detail screens.
 */
import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { colors, spacing, typography } from '../styles/globalStyles';

/**
 * @interface DetailHeaderProps
 * @description Props for the DetailHeader component.
 * @property {string} title - The title to be displayed in the header.
 */
interface DetailHeaderProps {
  title: string;
}

/**
 * @component DetailHeader
 * @description A header component for detail screens, featuring a back button and a title.
 * @param {DetailHeaderProps} props - The props for the component.
 */
const DetailHeader: React.FC<DetailHeaderProps> = ({ title }) => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      {/* Back button to navigate to the previous screen */}
      <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
        <Ionicons name="arrow-back" size={24} color={colors.primary} />
      </TouchableOpacity>
      <Text style={styles.title}>{title}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    backgroundColor: colors.background,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  backButton: {
    padding: spacing.sm,
  },
  title: {
    ...typography.h3,
    color: colors.textDark,
    marginLeft: spacing.md,
  },
});

export default DetailHeader;