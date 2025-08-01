import React, { useState, useCallback } from 'react';
import { View, FlatList, RefreshControl, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useFilteredYogaClasses } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage, YogaClassCard, EmptyState, SearchBar } from '../components';
import { RootStackParamList, YogaClass, SearchFilters } from '../types';
import { globalStyles, colors, spacing } from '../styles/globalStyles';

type ClassListNavigationProp = StackNavigationProp<RootStackParamList>;

export default function ClassListScreen() {
  const navigation = useNavigation<ClassListNavigationProp>();
  const { classes, loading, refresh, setFilters } = useFilteredYogaClasses();
  const [hasSearched, setHasSearched] = useState(false);

  const handleClassPress = (yogaClass: YogaClass) => {
    navigation.navigate('ClassDetail', { 
      classId: yogaClass.firebaseKey || yogaClass.id?.toString() || '',
      courseFirebaseKey: yogaClass.courseFirebaseKey
    });
  };

  const handleSearch = useCallback((filters: SearchFilters) => {
    setFilters(filters);
    setHasSearched(Object.values(filters).some(v => v));
  }, [setFilters]);

  const renderClassItem = ({ item }: { item: YogaClass }) => (
    <YogaClassCard 
      yogaClass={item} 
      onPress={() => handleClassPress(item)} 
    />
  );

  if (loading.isLoading && classes.length === 0) {
    return <LoadingSpinner message="Loading yoga classes..." />;
  }

  if (loading.error) {
    return (
      <ErrorMessage 
        message={loading.error}
        onRetry={refresh}
        retryText="Reload Classes"
      />
    );
  }

  const renderContent = () => {
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
    paddingVertical: spacing.sm,
    paddingBottom: spacing.lg,
  },
}); 