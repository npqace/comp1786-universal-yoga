/**
 * @file MyBookingsScreen.tsx
 * @description Screen to display the current user's booked classes.
 */
import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useUserBookings } from '../hooks/useYogaData';
import { useNetworkStatus } from '../hooks/useNetworkStatus';
import { LoadingSpinner, ErrorMessage, EmptyState } from '../components';
import { Booking } from '../types';
import { globalStyles, colors, spacing, typography, borderRadius } from '../styles/globalStyles';
import { Ionicons } from '@expo/vector-icons';
import { YogaService } from '../services/yogaService';
import { useToast } from '../context/ToastContext';

// Type definitions for navigation
type RootStackParamList = {
  ClassDetail: { classId: string };
};

type MyBookingsScreenNavigationProp = StackNavigationProp<RootStackParamList, 'ClassDetail'>;

/**
 * @screen MyBookingsScreen
 * @description This screen displays a list of classes that the current user has booked.
 * It allows users to view their bookings and cancel them.
 */
const MyBookingsScreen = () => {
  const { bookings, loading, refresh } = useUserBookings();
  const isOffline = useNetworkStatus();
  const yogaService = YogaService.getInstance();
  const navigation = useNavigation<MyBookingsScreenNavigationProp>();
  const { showToast } = useToast();

  /**
   * @method handleCancelBooking
   * @description Handles the cancellation of a booking after confirming with the user.
   * @param {string} classId - The ID of the class to cancel.
   */
  const handleCancelBooking = (classId: string) => {
    Alert.alert(
      "Confirm Cancellation",
      "Are you sure you want to cancel this booking?",
      [
        { text: "No", style: "cancel" },
        { 
          text: "Yes, Cancel", 
          style: "destructive", 
          onPress: async () => {
            try {
              await yogaService.cancelBooking(classId);
              showToast('Booking cancelled successfully.', 'success');
              // The real-time listener in useUserBookings will automatically update the list
            } catch (error) {
              const errorMessage = error instanceof Error ? error.message : "An unknown error occurred.";
              Alert.alert("Cancellation Failed", errorMessage);
            }
          }
        }
      ]
    );
  };

  /**
   * @method handleCardPress
   * @description Navigates to the ClassDetail screen for the selected booking.
   * @param {string} classId - The ID of the class associated with the booking.
   */
  const handleCardPress = (classId: string) => {
    navigation.navigate('ClassDetail', { classId });
  };

  // Calculate the total price of all bookings
  const totalPrice = bookings.reduce((total, booking) => total + (booking.price || 0), 0);

  // Show loading spinner on initial load
  if (loading.isLoading && bookings.length === 0) {
    return <LoadingSpinner message="Loading your bookings..." />;
  }

  // Show error message if fetching fails
  if (loading.error) {
    return <ErrorMessage message={loading.error} onRetry={refresh} />;
  }

  /**
   * @method renderBookingItem
   * @description Renders a single booking item in the FlatList.
   * @param {{ item: Booking }} props - The booking item to render.
   * @returns {React.ReactElement} A touchable card representing a booking.
   */
  const renderBookingItem = ({ item }: { item: Booking }) => (
    <TouchableOpacity onPress={() => handleCardPress(item.classId)} style={styles.bookingCard}>
      <View style={styles.accentBar} />
      <View style={styles.cardContent}>
        <View style={styles.cardHeader}>
          <Text style={styles.className}>{item.className || 'Class Name Not Available'}</Text>
          {item.price !== undefined && (
            <Text style={styles.priceText}>{`£${item.price.toFixed(2)}`}</Text>
          )}
        </View>
        
        <View style={styles.dateTimeContainer}>
          <View style={styles.detailRow}>
            <Ionicons name="calendar-outline" size={16} color={colors.primary} />
            <Text style={styles.detailText}>{item.classDate || 'Date N/A'}</Text>
          </View>
          <View style={styles.detailRow}>
            <Ionicons name="time-outline" size={16} color={colors.primary} />
            <Text style={styles.detailText}>{item.classTime || 'Time N/A'}</Text>
          </View>
        </View>

        <View style={styles.separator} />

        <View style={styles.detailRow}>
          <Ionicons name="person-outline" size={16} color={colors.textLight} />
          <Text style={styles.detailTextMuted}>{item.userName || 'User N/A'}</Text>
        </View>
        
        <Text style={styles.bookingDate}>
          Booked on {new Date(item.bookingDate).toLocaleDateString()}
        </Text>

        {/* Show cancel button only for active classes */}
        {item.classStatus?.toLowerCase() === 'active' && (
          <TouchableOpacity 
            style={[styles.cancelButton, isOffline && styles.disabledButton]} 
            disabled={isOffline}
            onPress={(e) => {
              e.stopPropagation(); // Prevent card press from firing when the button is pressed
              handleCancelBooking(item.classId);
            }}
          > 
            <Ionicons name="close-circle-outline" size={22} color={isOffline ? colors.textLight : colors.surface} />
            <Text style={[styles.cancelButtonText, isOffline && styles.disabledButtonText]}>
              {isOffline ? 'Offline' : 'Cancel Booking'}
            </Text>
          </TouchableOpacity>
        )}
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {bookings.length === 0 && !loading.isLoading ? (
        // Show empty state if there are no bookings
        <EmptyState 
          title="No Bookings"
          icon="sad-outline"
          message="You haven't booked any classes yet."
        />
      ) : (
        <>
          <FlatList
            data={bookings}
            keyExtractor={(item) => item.id}
            renderItem={renderBookingItem}
            onRefresh={refresh}
            refreshing={loading.isLoading}
            contentContainerStyle={styles.listContainer}
          />
          {/* Footer with total price */}
          {bookings.length > 0 && (
            <View style={styles.footerContainer}>
              <Text style={styles.totalPriceLabel}>Total Price:</Text>
              <Text style={styles.totalPriceValue}>{`£${totalPrice.toFixed(2)}`}</Text>
            </View>
          )}
        </>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  listContainer: {
    padding: spacing.md,
    paddingBottom: spacing.md,
  },
  bookingCard: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.large,
    marginBottom: spacing.md,
    flexDirection: 'row',
    overflow: 'hidden',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  accentBar: {
    width: 6,
    backgroundColor: colors.primary,
  },
  cardContent: {
    flex: 1,
    padding: spacing.md,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.sm,
  },
  className: {
    ...typography.h3,
    color: colors.textDark,
    flex: 1,
  },
  priceText: {
    ...typography.h3,
    color: colors.primary,
    fontWeight: 'bold',
  },
  dateTimeContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: spacing.md,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  detailText: {
    ...typography.body,
    color: colors.text,
    marginLeft: spacing.sm,
    fontWeight: '600',
  },
  detailTextMuted: {
    ...typography.body,
    color: colors.textLight,
    marginLeft: spacing.sm,
  },
  separator: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: spacing.sm,
  },
  bookingDate: {
    ...typography.caption,
    color: colors.textLight,
    marginTop: spacing.md,
    textAlign: 'right',
  },
  cancelButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.sm,
    marginTop: spacing.md,
    backgroundColor: colors.error,
    borderRadius: borderRadius.medium,
    borderWidth: 1,
    borderColor: colors.error,
  },
  cancelButtonText: {
    ...typography.body,
    color: colors.surface,
    marginLeft: spacing.sm,
    fontWeight: 'bold',
  },
  disabledButton: {
    backgroundColor: colors.disabled,
    borderColor: colors.border,
  },
  disabledButtonText: {
    color: colors.textLight,
  },
  footerContainer: {
    padding: spacing.lg,
    paddingBottom: spacing.md,
    backgroundColor: colors.surface,
    borderTopWidth: 1,
    borderColor: colors.border,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  totalPriceLabel: {
    ...typography.h3,
    color: colors.text,
  },
  totalPriceValue: {
    ...typography.h2,
    color: colors.primary,
    fontWeight: 'bold',
  },
});

export default MyBookingsScreen;