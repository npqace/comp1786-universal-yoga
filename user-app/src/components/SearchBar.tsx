import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Picker } from '@react-native-picker/picker';
import { SearchFilters } from '../types';
import { colors, globalStyles, spacing, typography, borderRadius } from '../styles/globalStyles';

interface SearchBarProps {
  onSearch: (filters: SearchFilters) => void;
  loading?: boolean;
}

const DAYS_OF_WEEK = [
  'All Days',
  'Monday',
  'Tuesday', 
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday'
];

const TIME_OPTIONS = [
  'Any Time',
  'Morning (6:00-12:00)',
  'Afternoon (12:00-18:00)',
  'Evening (18:00-22:00)'
];

export default function SearchBar({ onSearch, loading = false }: SearchBarProps) {
  const [selectedDay, setSelectedDay] = useState('All Days');
  const [selectedTime, setSelectedTime] = useState('Any Time');
  const [searchText, setSearchText] = useState('');
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);

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
    return filters;
  }, [searchText, selectedDay, selectedTime]);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      onSearch(buildFilters());
    }, 300); // Debounce search for 300ms

    return () => clearTimeout(delayDebounceFn);
  }, [searchText, selectedDay, selectedTime, onSearch, buildFilters]);

  const handleClear = () => {
    setSelectedDay('All Days');
    setSelectedTime('Any Time');
    setSearchText('');
    // onSearch will be triggered by useEffect after state updates
  };

  return (
    <View style={styles.container}>
      {/* Search input and Filter Toggle */}
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

      {/* Filters */}
      {showAdvancedFilters && (
        <View style={styles.filtersContainer}>
          <View style={styles.filterGroup}>
            <Text style={styles.filterLabel}>Day of Week</Text>
            <View style={styles.pickerContainer}>
              <Picker
                selectedValue={selectedDay}
                onValueChange={(itemValue) => setSelectedDay(itemValue)}
                style={styles.picker}
              >
                {DAYS_OF_WEEK.map(day => (
                  <Picker.Item key={day} label={day} value={day} />
                ))}
              </Picker>
            </View>
          </View>

          <View style={styles.filterGroup}>
            <Text style={styles.filterLabel}>Time of Day</Text>
            <View style={styles.pickerContainer}>
              <Picker
                selectedValue={selectedTime}
                onValueChange={(itemValue) => setSelectedTime(itemValue)}
                style={styles.picker}
              >
                {TIME_OPTIONS.map(time => (
                  <Picker.Item key={time} label={time} value={time} />
                ))}
              </Picker>
            </View>
          </View>

          {/* Clear button for advanced filters */}
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
  pickerContainer: {
    backgroundColor: colors.background,
    borderRadius: borderRadius.medium,
    borderWidth: 1,
    borderColor: colors.border,
  },
  picker: {
    height: 50,
    color: colors.text,
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