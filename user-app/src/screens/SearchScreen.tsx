import React, { useCallback } from 'react';
import { View, FlatList, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useClassSearch } from '../hooks/useYogaData';
import { SearchBar, YogaClassCard, EmptyState, ErrorMessage, LoadingSpinner } from '../components';
import { RootStackParamList, YogaClass, SearchFilters } from '../types';
import { globalStyles, colors, spacing } from '../styles/globalStyles';

type SearchNavigationProp = StackNavigationProp<RootStackParamList>;

export default function SearchScreen() {
  const navigation = useNavigation<SearchNavigationProp>();
  const { searchResults, loading, searchClasses } = useClassSearch();

  const handleSearch = useCallback((filters: SearchFilters) => {
    searchClasses(filters);
  }, [searchClasses]);

  const handleClassPress = (yogaClass: YogaClass) => {
    navigation.navigate('ClassDetail', { 
      classId: yogaClass.firebaseKey || yogaClass.id?.toString() || '',
      courseFirebaseKey: yogaClass.courseFirebaseKey
    });
  };

  const renderClassItem = ({ item }: { item: YogaClass }) => (
    <YogaClassCard 
      yogaClass={item} 
      onPress={() => handleClassPress(item)} 
    />
  );

  const renderContent = () => {
    if (loading.isLoading) {
      return <LoadingSpinner message="Searching classes..." />;
    }

    if (loading.error) {
      return (
        <ErrorMessage 
          message={loading.error}
          onRetry={() => {
            searchClasses({}); // Re-trigger search with current filters
          }}
          retryText="Try Search Again"
        />
      );
    }

    if (searchResults.length === 0) {
      return (
        <EmptyState
          icon="search-outline"
          title="No Classes Found"
          message="No yoga classes match your search criteria. Try adjusting your filters or searching for different criteria."
        />
      );
    }

    return (
      <FlatList
        data={searchResults}
        renderItem={renderClassItem}
        keyExtractor={(item, index) => item.firebaseKey || item.id?.toString() || index.toString()}
        contentContainerStyle={styles.listContainer}
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