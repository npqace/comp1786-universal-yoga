import React from 'react';
import { View, FlatList, RefreshControl, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useYogaClasses } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage, YogaClassCard, EmptyState } from '../components';
import { RootStackParamList, YogaClass } from '../types';
import { globalStyles, colors, spacing } from '../styles/globalStyles';

type ClassListNavigationProp = StackNavigationProp<RootStackParamList>;

export default function ClassListScreen() {
  const navigation = useNavigation<ClassListNavigationProp>();
  const { classes, loading, refresh } = useYogaClasses();

  const handleClassPress = (yogaClass: YogaClass) => {
    navigation.navigate('ClassDetail', { 
      classId: yogaClass.firebaseKey || yogaClass.id?.toString() || '',
      courseId: yogaClass.courseId?.toString()
    });
  };

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

  if (classes.length === 0 && !loading.isLoading) {
    return (
      <EmptyState
        icon="fitness-outline"
        title="No Classes Available"
        message="There are currently no yoga classes scheduled. Please check back later or contact your instructor."
      />
    );
  }

  return (
    <View style={[globalStyles.container, styles.container]}>
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.background,
  },
  listContainer: {
    paddingVertical: spacing.sm,
    paddingBottom: spacing.lg,
  },
}); 