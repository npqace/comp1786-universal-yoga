/**
 * @file CustomHeader.tsx
 * @description A reusable header component for the application.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../styles/globalStyles';
import Logo from '../../assets/yoga-logo.svg';

/**
 * @interface CustomHeaderProps
 * @description Props for the CustomHeader component.
 * @property {string} [title] - The optional title to display in the header. Defaults to 'Universal Yoga'.
 */
interface CustomHeaderProps {
  title?: string;
}

/**
 * @component CustomHeader
 * @description A reusable header component that displays the app logo and a title.
 * @param {CustomHeaderProps} props - The props for the component.
 */
const CustomHeader: React.FC<CustomHeaderProps> = ({ title }) => {
  return (
    <View style={styles.headerContainer}>
      <View style={styles.headerCard}>
        <Logo width={48} height={48} style={styles.logo} />
        <View style={styles.textContainer}>
          <Text style={styles.title}>{title || 'Universal Yoga'}</Text>
          <Text style={styles.subtitle}>Your Path to Wellness</Text>
        </View>
        {/* Future implementation for a menu icon can be added here */}
        {/* <Ionicons name="menu" size={24} color="white" /> */}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  headerContainer: {
    padding: 16,
    // backgroundColor: colors.primary,
    backgroundColor: colors.background,
  },
  headerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.primary,
    borderRadius: 10,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  logo: {
    marginRight: 16,
  },
  textContainer: {
    flex: 1,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    color: '#E0E0E0',
  },
});

export default CustomHeader;