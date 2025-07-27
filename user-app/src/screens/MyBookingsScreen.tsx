import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { useUserBookings } from '../hooks/useYogaData';
import { LoadingSpinner, ErrorMessage } from '../components';
import { YogaClassCard } from '../components';
import { YogaService } from '../services/yogaService';
import { YogaClass, Booking } from '../types';

import { globalStyles, colors, spacing } from '../styles/globalStyles';

const MyBookingsScreen = () => {
  const { bookings, loading, refresh } = useUserBookings();
  const [classes, setClasses] = React.useState<YogaClass[]>([]);
  const yogaService = YogaService.getInstance();

  React.useEffect(() => {
    const fetchClasses = async () => {
      if (bookings.length > 0) {
        const classIds = bookings.map((b: Booking) => b.classId);
        const allClasses = await yogaService.getClassesWithCourses();
        const bookedClasses = allClasses.filter(c => c.firebaseKey && classIds.includes(c.firebaseKey));
        setClasses(bookedClasses);
      }
    };
    fetchClasses();
  }, [bookings]);

  if (loading.isLoading) {
    return <LoadingSpinner message="Loading your bookings..." />;
  }

  if (loading.error) {
    return <ErrorMessage message={loading.error} onRetry={refresh} />;
  }

  return (
    <View style={globalStyles.container}>
      {classes.length === 0 ? (
        <Text>You have no upcoming bookings.</Text>
      ) : (
        <FlatList
          data={classes}
          keyExtractor={(item) => item.firebaseKey || (item.id ? item.id.toString() : Math.random().toString())}
          renderItem={({ item }) => <YogaClassCard yogaClass={item} onPress={() => {}} />}
          onRefresh={refresh}
          refreshing={loading.isLoading}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  // You can keep specific styles here if needed, or remove if everything is in globalStyles
});

export default MyBookingsScreen;
