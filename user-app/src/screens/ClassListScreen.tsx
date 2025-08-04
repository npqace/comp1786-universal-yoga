/**
 * @file ClassListScreen.tsx
 * @description Screen to display a list of available yoga classes with search and filter capabilities.
 */
import React, { useState, useCallback } from 'react';
import { View, FlatList, RefreshControl, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useFilteredYogaClasses } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage, YogaClassCard, EmptyState, SearchBar } from '../components';
import { RootStackParamList, YogaClass, SearchFilters } from '../types';
import { globalStyles, colors, spacing } from '../styles/globalStyles';

// Type for the navigation prop
type ClassListNavigationProp = StackNavigationProp<RootStackParamList>;

/**
 * @screen ClassListScreen
 * @description This screen fetches and displays a list of yoga classes.
 * It uses the `useFilteredYogaClasses` hook to handle data fetching, filtering, and state management.
 * It also includes a search bar and pull-to-refresh functionality.
 */
export default function ClassListScreen() {
  const navigation = useNavigation<ClassListNavigationProp>();
  const { classes, loading, refresh, setFilters } = useFilteredYogaClasses();
  const [hasSearched, setHasSearched] = useState(false);

  /**
   * @method handleClassPress
   * @description Navigates to the ClassDetail screen when a class card is pressed.
   * @param {YogaClass} yogaClass - The class that was pressed.
   */
  const handleClassPress = (yogaClass: YogaClass) => {
    navigation.navigate('ClassDetail', { 
      classId: yogaClass.firebaseKey || yogaClass.id?.toString() || '',
      courseFirebaseKey: yogaClass.courseFirebaseKey
    });
  };

  /**
   * @method handleSearch
   * @description Callback function passed to the SearchBar component.
   * It updates the filters in the `useFilteredYogaClasses` hook.
   * @param {SearchFilters} filters - The search filters from the SearchBar.
   */
  const handleSearch = useCallback((filters: SearchFilters) => {
    setFilters(filters);
    // Track if a search has been performed to show the correct empty state message
    setHasSearched(Object.values(filters).some(v => v));
  }, [setFilters]);

  /**
   * @method renderClassItem
   * @description Renders a single yoga class item in the FlatList.
   * @param {{ item: YogaClass }} props - The item to render.
   * @returns {React.ReactElement} A YogaClassCard component.
   */
  const renderClassItem = ({ item }: { item: YogaClass }) => (
    <YogaClassCard 
      yogaClass={item} 
      onPress={() => handleClassPress(item)} 
    />
  );

  // Show a loading spinner on initial load
  if (loading.isLoading && classes.length === 0) {
    return <LoadingSpinner message="Loading yoga classes..." />;
  }

  // Show an error message if data fetching fails
  if (loading.error) {
    return (
      <ErrorMessage 
        message={loading.error}
        onRetry={refresh}
        retryText="Reload Classes"
      />
    );
  }

  /**
   * @method renderContent
   * @description Renders the main content of the screen, which is either the list of classes or an empty state message.
   * @returns {React.ReactElement} The FlatList or EmptyState component.
   */
  const renderContent = () => {
    // Show an empty state message if there are no classes
    if (classes.length === 0 && !loading.isLoading) {
      return (
        <EmptyState
          icon={hasSearched ? "search-outline" : "fitness-outline"}
          title={hasSearched ? "No Classes Found" : "No Classes Available"}
          message={
            hasSearched
              ? "No yoga classes match your search criteria. Try adjusting your filters."
              : "There are currently no yoga classes scheduled. Please check back later."
          }
        />
      );
    }

    // Render the list of classes
    return (
      <FlatList
        data={classes}
        renderItem={renderClassItem}
        keyExtractor={(item, index) => item.firebaseKey || item.id?.toString() || index.toString()}
        contentContainerStyle={styles.listContainer}
        refreshControl={
          <RefreshControl
            refreshing={loading.isLoading}
            onRefresh={refresh}
            colors={[colors.primary]}
            tintColor={colors.primary}
          />
        }
        showsVerticalScrollIndicator={false}
      />
    );
  };

  return (
    <View style={[globalStyles.container, styles.container]}>
      {/* SearchBar is placed with a higher zIndex to appear above the list */}
      <View style={{ zIndex: 1 }}>
        <SearchBar 
          onSearch={handleSearch}
          loading={loading.isLoading}
        />
      </View>
      <View style={styles.contentContainer}>
        {renderContent()}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.background,
  },
  contentContainer: {
    flex: 1,
  },
  listContainer: {
    paddingBottom: spacing.sm,
  },
});