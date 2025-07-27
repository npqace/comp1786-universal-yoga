import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import CustomDropdown from './CustomDropdown'; // Import the new custom component
import { SearchFilters } from '../types';
import { colors, globalStyles, spacing, typography, borderRadius } from '../styles/globalStyles';
import { useYogaCourses } from '../hooks/useYogaData';

interface SearchBarProps {
  onSearch: (filters: SearchFilters) => void;
  loading?: boolean;
}

const DAYS_OF_WEEK = [
  { label: 'All Days', value: 'All Days' },
  { label: 'Monday', value: 'Monday' },
  { label: 'Tuesday', value: 'Tuesday' },
  { label: 'Wednesday', value: 'Wednesday' },
  { label: 'Thursday', value: 'Thursday' },
  { label: 'Friday', value: 'Friday' },
  { label: 'Saturday', value: 'Saturday' },
  { label: 'Sunday', value: 'Sunday' }
];

const TIME_OPTIONS = [
  { label: 'Any Time', value: 'Any Time' },
  { label: 'Morning (6:00-12:00)', value: 'Morning (6:00-12:00)' },
  { label: 'Afternoon (12:00-18:00)', value: 'Afternoon (12:00-18:00)' },
  { label: 'Evening (18:00-22:00)', value: 'Evening (18:00-22:00)' }
];

export default function SearchBar({ onSearch, loading = false }: SearchBarProps) {
  const [selectedDay, setSelectedDay] = useState('All Days');
  const [selectedTime, setSelectedTime] = useState('Any Time');
  const [selectedCourse, setSelectedCourse] = useState('All Courses');
  const [searchText, setSearchText] = useState('');
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);

  const { courses, loading: coursesLoading } = useYogaCourses();

  const courseOptions = useMemo(() => {
    const options = courses.map(course => ({
      label: course.classType,
      value: course.firebaseKey, // Always use the firebaseKey for the value
    }));
    return [{ label: 'All Courses', value: 'All Courses' }, ...options];
  }, [courses]);

  const buildFilters = useCallback(() => {
    const filters: SearchFilters = {};
    
    if (searchText) {
      filters.name = searchText;
    }

    if (selectedDay !== 'All Days') {
      filters.dayOfWeek = selectedDay;
    }
    
    if (selectedTime !== 'Any Time') {
      if (selectedTime.includes('Morning')) filters.timeOfDay = 'morning';
      else if (selectedTime.includes('Afternoon')) filters.timeOfDay = 'afternoon';
      else if (selectedTime.includes('Evening')) filters.timeOfDay = 'evening';
    }

    if (selectedCourse !== 'All Courses') {
      filters.courseId = selectedCourse;
    }

    return filters;
  }, [searchText, selectedDay, selectedTime, selectedCourse]);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      onSearch(buildFilters());
    }, 300); // Debounce search for 300ms

    return () => clearTimeout(delayDebounceFn);
  }, [searchText, selectedDay, selectedTime, selectedCourse, onSearch, buildFilters]);

  const handleClear = () => {
    setSelectedDay('All Days');
    setSelectedTime('Any Time');
    setSelectedCourse('All Courses');
    setSearchText('');
  };

  return (
    <View style={styles.container}>
      <View style={styles.searchInputContainer}>
        <Ionicons name="search-outline" size={20} color={colors.textLight} />
        <TextInput
          style={styles.searchInput}
          placeholder="Search classes..."
          value={searchText}
          onChangeText={setSearchText}
          placeholderTextColor={colors.textLight}
        />
        {searchText.length > 0 && (
          <TouchableOpacity onPress={() => setSearchText('')} style={styles.clearTextButton}>
            <Ionicons name="close-circle-outline" size={20} color={colors.textLight} />
          </TouchableOpacity>
        )}
        <TouchableOpacity 
          style={styles.filterToggleButton} 
          onPress={() => setShowAdvancedFilters(!showAdvancedFilters)}
        >
          <Ionicons name="options-outline" size={24} color={colors.primary} />
        </TouchableOpacity>
      </View>

      {showAdvancedFilters && (
        <View style={styles.filtersContainer}>
          <View style={styles.filterGroup}>
            <Text style={styles.filterLabel}>Yoga Course</Text>
            <CustomDropdown
              value={selectedCourse}
              data={courseOptions}
              onSelect={(item) => setSelectedCourse(item.value)}
              defaultButtonText="All Courses"
              />
          </View>

          <View style={styles.filterGroup}>
            <Text style={styles.filterLabel}>Day of Week</Text>
            <CustomDropdown
              value={selectedDay}
              data={DAYS_OF_WEEK}
              onSelect={(item) => setSelectedDay(item.value)}
              defaultButtonText="All Days"
            />
          </View>

          <View style={styles.filterGroup}>
            <Text style={styles.filterLabel}>Time of Day</Text>
            <CustomDropdown
              value={selectedTime}
              data={TIME_OPTIONS}
              onSelect={(item) => setSelectedTime(item.value)}
              defaultButtonText="Any Time"
            />
          </View>

          <TouchableOpacity 
            style={[styles.button, styles.clearButton]} 
            onPress={handleClear}
          >
            <Ionicons name="refresh-outline" size={18} color={colors.textLight} />
            <Text style={styles.clearButtonText}>Clear Filters</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surface,
    padding: spacing.md,
    marginHorizontal: spacing.md,
    marginVertical: spacing.sm,
    borderRadius: borderRadius.medium,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.22,
    shadowRadius: 2.22,
  },
  searchInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.background,
    borderRadius: borderRadius.medium,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  searchInput: {
    flex: 1,
    marginLeft: spacing.sm,
    fontSize: 16,
    color: colors.text,
  },
  clearTextButton: {
    padding: spacing.xs,
  },
  filterToggleButton: {
    paddingLeft: spacing.md,
  },
  filtersContainer: {
    marginTop: spacing.md,
  },
  filterGroup: {
    marginBottom: spacing.sm,
  },
  filterLabel: {
    ...typography.bodySmall,
    color: colors.text,
    fontWeight: '600',
    marginBottom: spacing.xs,
  },
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.lg,
    borderRadius: borderRadius.medium,
    marginTop: spacing.md,
  },
  clearButton: {
    backgroundColor: colors.background,
    borderWidth: 1,
    borderColor: colors.border,
  },
  clearButtonText: {
    color: colors.textLight,
    fontSize: 16,
    fontWeight: '600',
    marginLeft: spacing.xs,
  },
});
