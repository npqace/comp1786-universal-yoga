import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { RouteProp, useRoute } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { useClassDetail } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage } from '../components';
import DetailItem from '../components/DetailItem';
import { RootStackParamList, formatPrice, getDayOfWeek } from '../types';
import { globalStyles, colors, spacing, typography, borderRadius } from '../styles/globalStyles';

type ClassDetailRouteProp = RouteProp<RootStackParamList, 'ClassDetail'>;

export default function ClassDetailScreen() {
  const route = useRoute<ClassDetailRouteProp>();
  const { classId } = route.params;
  const { classDetail, loading, refresh } = useClassDetail(classId);

  if (loading.isLoading) {
    return <LoadingSpinner message="Loading class details..." />;
  }

  if (loading.error) {
    return (
      <ErrorMessage 
        message={loading.error}
        onRetry={refresh}
        retryText="Reload Details"
      />
    );
  }

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

  return (
    <ScrollView style={[globalStyles.container, styles.container]}>
      {/* Header Card */}
      <View style={[globalStyles.card, styles.headerCard]}>
        <View style={styles.headerContent}>
          <Text style={styles.classType}>{course.classType}</Text>
          <View style={styles.priceStatusContainer}>
            <Text style={styles.price}>{formatPrice(course.price)}</Text>
            <View style={[styles.statusContainer, { backgroundColor: colors.primary }]}>
              <Text style={styles.statusText}>
                {classDetail.status?.toUpperCase() || 'SCHEDULED'}
              </Text>
            </View>
          </View>
        </View>
      </View>

      {/* Class Details */}
      <View style={[globalStyles.card, styles.detailsCard]}>
        <Text style={styles.sectionTitle}>Class Information</Text>
        <DetailItem icon="calendar-outline" label="Date" value={classDetail.date} />
        <DetailItem icon="calendar-number-outline" label="Day of Week" value={dayOfWeek} />
        <DetailItem icon="time-outline" label="Time" value={course.time} />
        <DetailItem icon="person-outline" label="Instructor" value={classDetail.assignedInstructor} />
        <DetailItem icon="hourglass-outline" label="Duration" value={course.duration} unit="minutes" />
        <DetailItem icon="people-outline" label="Capacity" value={classDetail.actualCapacity || course.capacity} unit="people" />
        <DetailItem icon="location-outline" label="Room" value={course.roomNumber ? `Room ${course.roomNumber}` : ''} />
        <DetailItem icon="trending-up-outline" label="Difficulty" value={course.difficultyLevel} />
        <DetailItem icon="people-circle-outline" label="Age Group" value={course.ageGroup} />
      </View>

      {/* Description */}
      <View style={[globalStyles.card, styles.descriptionCard]}>
        <Text style={styles.sectionTitle}>Description</Text>
        <Text style={styles.description}>{course.description}</Text>
      </View>

      {/* Equipment */}
      <View style={[globalStyles.card, styles.equipmentCard]}>
        <Text style={styles.sectionTitle}>Equipment Needed</Text>
        <Text style={styles.equipment}>{course.equipmentNeeded}</Text>
      </View>

      {/* Additional Comments */}
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
  statusContainer: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: borderRadius.small,
  },
  statusText: {
    ...typography.bodySmall,
    color: colors.surface,
    fontWeight: '600',
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
}); 