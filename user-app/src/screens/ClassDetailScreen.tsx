/**
 * @file ClassDetailScreen.tsx
 * @description Screen to display the detailed information of a specific yoga class.
 */
import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { RouteProp, useRoute } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { useClassDetail } from '../hooks/useYogaData';
import { useNetworkStatus } from '../hooks/useNetworkStatus';
import { LoadingSpinner, ErrorMessage } from '../components';
import DetailItem from '../components/DetailItem';
import { RootStackParamList, getDayOfWeek } from '../types';
import { globalStyles, colors, spacing, typography } from '../styles/globalStyles';
import { YogaService } from '../services/yogaService';
import { useToast } from '../context/ToastContext';

// Type for the navigation route parameters
type ClassDetailRouteProp = RouteProp<RootStackParamList, 'ClassDetail'>;

/**
 * @screen ClassDetailScreen
 * @description Displays all details for a selected yoga class and allows the user to book it.
 */
export default function ClassDetailScreen() {
  const route = useRoute<ClassDetailRouteProp>();
  const { classId } = route.params;
  const { classDetail, isBooked, loading, refresh } = useClassDetail(classId);
  const isOffline = useNetworkStatus();
  const yogaService = YogaService.getInstance();
  const { showToast } = useToast();

  /**
   * @method handleBooking
   * @description Handles the class booking process.
   */
  const handleBooking = async () => {
    if (!classDetail?.firebaseKey || !isBookable) return;

    try {
      await yogaService.bookClass(classDetail.firebaseKey);
      showToast('You have successfully booked this class.', 'success');
      refresh(); // Refresh to update the UI (e.g., isBooked state, slots available)
    } catch (error) {
      Alert.alert('Error', error instanceof Error ? error.message : 'An unknown error occurred.');
    }
  };

  // Show loading spinner while fetching data
  if (loading.isLoading) {
    return <LoadingSpinner message="Loading class details..." />;
  }

  // Show error message if fetching fails
  if (loading.error) {
    return (
      <ErrorMessage 
        message={loading.error}
        onRetry={refresh}
        retryText="Reload Details"
      />
    );
  }

  // Show error message if class details are not found
  if (!classDetail || !classDetail.course) {
    return (
      <ErrorMessage 
        message="Class details not found"
        onRetry={refresh}
        retryText="Try Again"
      />
    );
  }

  const course = classDetail.course;
  const dayOfWeek = getDayOfWeek(classDetail.date);
  // A class is bookable if it's active and has slots available
  const isBookable = classDetail.status?.toLowerCase() === 'active' && classDetail.slotsAvailable > 0;

  /**
   * @method getButtonText
   * @description Determines the text to display on the booking button based on the class and user status.
   * @returns {string} The text for the button.
   */
  const getButtonText = () => {
    if (isOffline) return 'Offline: Cannot Book';
    if (isBooked) return 'Already Booked';
    if (classDetail.status?.toLowerCase() === 'completed') return 'Class Completed';
    if (classDetail.status?.toLowerCase() === 'cancelled') return 'Class Cancelled';
    if (classDetail.slotsAvailable === 0) return 'Class Full';
    return 'Book Now';
  };

  return (
    <ScrollView style={[globalStyles.container, styles.container]}>
      {/* Class Details Card */}
      <View style={[globalStyles.card, styles.detailsCard]}>
        <Text style={styles.sectionTitle}>Class Information</Text>
        <DetailItem icon="calendar-outline" label="Date" value={classDetail.date} />
        <DetailItem icon="calendar-number-outline" label="Day of Week" value={dayOfWeek} />
        <DetailItem icon="time-outline" label="Time" value={course.time} />
        <DetailItem icon="person-outline" label="Instructor" value={classDetail.assignedInstructor} />
        <DetailItem icon="hourglass-outline" label="Duration" value={course.duration} unit="minutes" />
        <DetailItem icon="people-outline" label="Capacity" value={classDetail.actualCapacity || course.capacity} unit="people" />
        <DetailItem icon="checkmark-circle-outline" label="Slots Available" value={classDetail.slotsAvailable} unit="slots left" />
        <DetailItem icon="location-outline" label="Room" value={course.roomNumber ? `Room ${course.roomNumber}` : ''} />
        <DetailItem icon="trending-up-outline" label="Difficulty" value={course.difficultyLevel} />
        <DetailItem icon="people-circle-outline" label="Age Group" value={course.ageGroup} />
      </View>

      {/* Booking Button */}
      <View style={styles.bookingContainer}>
        <TouchableOpacity
          style={[
            globalStyles.button,
            styles.bookButton,
            // Disable button if not bookable, already booked, or offline
            (!isBookable || isBooked || isOffline) && styles.disabledButton
          ]}
          disabled={!isBookable || isBooked || isOffline}
          onPress={handleBooking}
        >
          <Text style={globalStyles.buttonText}>
            {getButtonText()}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Description Card */}
      <View style={[globalStyles.card, styles.descriptionCard]}>
        <Text style={styles.sectionTitle}>Description</Text>
        <Text style={styles.description}>{course.description}</Text>
      </View>

      {/* Equipment Card */}
      <View style={[globalStyles.card, styles.equipmentCard]}>
        <Text style={styles.sectionTitle}>Equipment Needed</Text>
        <Text style={styles.equipment}>{course.equipmentNeeded}</Text>
      </View>

      {/* Additional Comments Card */}
      <View style={[globalStyles.card, styles.commentsCard]}>
        <Text style={styles.sectionTitle}>Additional Notes</Text>
        <Text style={styles.comments}>{classDetail.additionalComments}</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.background,
  },
  headerCard: {
    marginTop: spacing.sm,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  classType: {
    ...typography.h2,
    color: colors.textDark,
    flex: 1,
    marginRight: spacing.md,
  },
  priceStatusContainer: {
    alignItems: 'flex-end',
  },
  price: {
    ...typography.h1,
    color: colors.primary,
    fontWeight: 'bold',
    marginBottom: spacing.xs,
  },
  detailsCard: {
    marginTop: 0,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.textDark,
    marginBottom: spacing.md,
  },
  descriptionCard: {
    marginTop: 0,
  },
  description: {
    ...typography.body,
    color: colors.text,
    lineHeight: 24,
  },
  equipmentCard: {
    marginTop: 0,
  },
  equipment: {
    ...typography.body,
    color: colors.text,
    lineHeight: 24,
  },
  commentsCard: {
    marginTop: 0,
    backgroundColor: colors.info + '10',
    borderLeftWidth: 4,
    borderLeftColor: colors.info,
    marginBottom: spacing.lg,
  },
  comments: {
    ...typography.body,
    color: colors.text,
    lineHeight: 24,
    fontStyle: 'italic',
  },
  bookingContainer: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.lg,
  },
  bookButton: {
    backgroundColor: colors.primary,
  },
  disabledButton: {
    backgroundColor: colors.disabled,
  },
});