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
    <View style={[globalStyles.card, styles.bookingCard]}>
      <View style={styles.cardHeader}>
        <Text style={styles.className}>{item.className || 'Class Name Not Available'}</Text>
      </View>
      <View style={styles.cardBody}>
        <View style={styles.detailRow}>
          <Ionicons name="calendar-outline" size={16} color={colors.text} />
          <Text style={styles.detailText}>{item.classDate || 'Date N/A'}</Text>
        </View>
        <View style={styles.detailRow}>
          <Ionicons name="time-outline" size={16} color={colors.text} />
          <Text style={styles.detailText}>{item.classTime || 'Time N/A'}</Text>
        </View>
        <View style={styles.detailRow}>
          <Ionicons name="person-outline" size={16} color={colors.text} />
          <Text style={styles.detailText}>{item.userName || 'User N/A'}</Text>
        </View>
      </View>
      <View style={styles.cardFooter}>
        <Text style={styles.bookingDate}>
          Booked on: {new Date(item.bookingDate).toLocaleDateString()}
        </Text>
      </View>
    </View>
  );

  return (
    <View style={globalStyles.container}>
      {bookings.length === 0 ? (
        <EmptyState 
          icon="sad-outline"
          message="No Bookings Yet"
          details="You haven't booked any classes. Explore our classes and find one that suits you!"
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
    marginBottom: spacing.md,
    backgroundColor: colors.surface,
    borderRadius: borderRadius.medium,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.41,
  },
  cardHeader: {
    padding: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  className: {
    ...typography.h3,
    color: colors.primary,
  },
  cardBody: {
    padding: spacing.md,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  detailText: {
    ...typography.body,
    color: colors.text,
    marginLeft: spacing.sm,
  },
  cardFooter: {
    padding: spacing.md,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    alignItems: 'center',
  },
  bookingDate: {
    ...typography.bodySmall,
    color: colors.textLight,
  },
});

export default MyBookingsScreen;
