import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { YogaClass, getDayOfWeek } from '../types';
import { formatPrice } from '../types/YogaCourse';
import { colors, globalStyles, spacing, typography, borderRadius } from '../styles/globalStyles';

interface YogaClassCardProps {
  yogaClass: YogaClass;
  onPress: () => void;
}

export default function YogaClassCard({ yogaClass, onPress }: YogaClassCardProps) {
  const course = yogaClass.course;

  // Extract day of week from date if course dayOfWeek is not available
  const dayOfWeek = course?.dayOfWeek || getDayOfWeek(yogaClass.date);
  
  // Use course classType or fallback to a generic name
  const classType = course?.classType || 'Yoga Class';
  
  // Get price or show as TBD
  const price = course?.price ? formatPrice(course.price) : 'TBD';

  const getStatusBackgroundColor = (status?: 'scheduled' | 'cancelled' | 'completed'): string => {
    switch (status) {
      case 'scheduled':
        return colors.primary;
      case 'completed':
        return colors.success;
      case 'cancelled':
        return colors.error;
      default:
        return colors.primary;
    }
  };

  return (
    <TouchableOpacity style={[globalStyles.card, styles.card]} onPress={onPress}>
      {/* Header: Class Type and Price/Status */}
      <View style={styles.header}>
        <Text style={styles.classType}>{classType}</Text>
        <View style={styles.priceStatusContainer}>
          <Text style={styles.price}>{price}</Text>
          <View style={[styles.statusContainer, { backgroundColor: getStatusBackgroundColor(yogaClass.status) }]}>
            <Text style={styles.statusText}>
              {yogaClass.status?.toUpperCase() || 'SCHEDULED'}
            </Text>
          </View>
        </View>
      </View>
      
      {/* Divider */}
      <View style={styles.divider} />

      {/* Class Details */}
      <View style={styles.detailsContainer}>
        <View style={styles.detailColumn}>
          <View style={styles.row}>
            <Ionicons name="calendar-outline" size={16} color={colors.primary} />
            <Text style={styles.detailText}>{yogaClass.date}</Text>
          </View>
          
          {dayOfWeek && (
            <View style={styles.row}>
              <Ionicons name="calendar-number-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{dayOfWeek}</Text>
            </View>
          )}
          
          {course?.time && (
            <View style={styles.row}>
              <Ionicons name="time-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{course.time}</Text>
            </View>
          )}

          {yogaClass.assignedInstructor && (
            <View style={styles.row}>
              <Ionicons name="person-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{yogaClass.assignedInstructor}</Text>
            </View>
          )}
        </View>

        <View style={styles.detailColumn}>
          {(yogaClass.actualCapacity || course?.capacity) && (
            <View style={styles.row}>
              <Ionicons name="people-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>
                Capacity: {yogaClass.actualCapacity || course?.capacity}
              </Text>
            </View>
          )}

          {course?.duration && (
            <View style={styles.row}>
              <Ionicons name="hourglass-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{course.duration} minutes</Text>
            </View>
          )}

          {course?.difficultyLevel && (
            <View style={styles.row}>
              <Ionicons name="trending-up-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{course.difficultyLevel}</Text>
            </View>
          )}

          {course?.ageGroup && (
            <View style={styles.row}>
              <Ionicons name="people-circle-outline" size={16} color={colors.primary} />
              <Text style={styles.detailText}>{course.ageGroup}</Text>
            </View>
          )}
        </View>
      </View>

      {course?.equipmentNeeded && (
        <View style={[styles.row, styles.fullWidthRow]}>
          <Ionicons name="barbell-outline" size={16} color={colors.primary} />
          <Text style={styles.detailText}>{course.equipmentNeeded}</Text>
        </View>
      )}

      {/* Description */}
      {course?.description && (
        <Text style={styles.description} numberOfLines={2}>
          {course.description}
        </Text>
      )}

      {/* Additional Comments */}
      {yogaClass.additionalComments && (
        <Text style={styles.description} numberOfLines={2}>
          {yogaClass.additionalComments}
        </Text>
      )}

      {/* Room info */}
      {course?.roomNumber && (
        <View style={styles.roomContainer}>
          <Ionicons name="location-outline" size={14} color={colors.textLight} />
          <Text style={styles.roomText}>Room {course.roomNumber}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    marginVertical: spacing.sm,
    elevation: 3,
    shadowOpacity: 0.1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  classType: {
    ...typography.h2,
    color: colors.textDark,
    flex: 1, 
    marginRight: spacing.sm,
  },
  priceStatusContainer: {
    alignItems: 'flex-end',
  },
  price: {
    ...typography.h2,
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
    ...typography.caption,
    color: colors.surface,
    fontWeight: '600',
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
    marginBottom: spacing.md,
  },
  detailsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: spacing.sm,
  },
  detailColumn: {
    width: '48%',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  fullWidthRow: {
    width: '100%',
  },
  detailText: {
    ...typography.bodySmall,
    color: colors.text,
    marginLeft: spacing.sm,
    flex: 1,
  },
  description: {
    ...typography.bodySmall,
    color: colors.textLight,
    marginBottom: spacing.sm,
    lineHeight: 20,
    fontStyle: 'italic',
  },
  roomContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: spacing.sm,
    alignSelf: 'flex-end',
  },
  roomText: {
    ...typography.caption,
    color: colors.textLight,
    marginLeft: spacing.xs,
  },
}); 