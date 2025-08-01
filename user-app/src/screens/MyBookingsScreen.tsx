import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { useUserBookings } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage, EmptyState } from '../components';
import { Booking } from '../types';
import { globalStyles, colors, spacing, typography, borderRadius } from '../styles/globalStyles';
import { Ionicons } from '@expo/vector-icons';

const MyBookingsScreen = () => {
  const { bookings, loading, refresh } = useUserBookings();

  if (loading.isLoading) {
    return <LoadingSpinner message="Loading your bookings..." />;
  }

  if (loading.error) {
    return <ErrorMessage message={loading.error} onRetry={refresh} />;
  }

  const renderBookingItem = ({ item }: { item: Booking }) => (
    <View style={styles.bookingCard}>
      <View style={styles.accentBar} />
      <View style={styles.cardContent}>
        <Text style={styles.className}>{item.className || 'Class Name Not Available'}</Text>
        
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
      </View>
    </View>
  );

  return (
    <View style={globalStyles.container}>
      {bookings.length === 0 ? (
        <EmptyState 
          title="No Bookings"
          icon="sad-outline"
          message="You haven't booked any classes yet."
        />
      ) : (
        <FlatList
          data={bookings}
          keyExtractor={(item) => item.id}
          renderItem={renderBookingItem}
          onRefresh={refresh}
          refreshing={loading.isLoading}
          contentContainerStyle={styles.listContainer}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  listContainer: {
    padding: spacing.md,
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
  className: {
    ...typography.h3,
    color: colors.textDark,
    marginBottom: spacing.sm,
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
});

export default MyBookingsScreen;
