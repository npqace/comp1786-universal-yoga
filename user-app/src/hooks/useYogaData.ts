import { useState, useEffect, useCallback, useMemo } from 'react';
import { YogaService } from '../services/yogaService';
import { YogaClass, LoadingState, SearchFilters, Booking } from '../types';
import { YogaCourse } from '../types/YogaCourse';

export function useFilteredYogaClasses() {
  const [allClasses, setAllClasses] = useState<YogaClass[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const [filters, setFilters] = useState<SearchFilters>({});
  const yogaService = YogaService.getInstance();

  const fetchClasses = useCallback(async () => {
    try {
      setLoading({ isLoading: true });
      const fetchedClasses = await yogaService.getClassesWithCourses();
      setAllClasses(fetchedClasses);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch classes' 
      });
    }
  }, [yogaService]);

  useEffect(() => {
    // Initial fetch
    fetchClasses();

    // Subscribe to real-time updates
    const unsubscribe = yogaService.subscribeToClassesUpdates(async () => {
      try {
        const classesWithCourses = await yogaService.getClassesWithCourses();
        setAllClasses(classesWithCourses);
      } catch (error) {
        console.error("Failed to process class updates:", error);
      }
    });

    return () => unsubscribe();
  }, [yogaService, fetchClasses]);

  const filteredClasses = useMemo(() => {
    const { name, dayOfWeek, timeOfDay, courseFirebaseKey } = filters;
    if (!name && !dayOfWeek && !timeOfDay && !courseFirebaseKey) {
      return allClasses;
    }

    return allClasses.filter(cls => {
      if (!cls.course) return false;
      let matches = true;
      if (name) {
        const lowerCaseName = name.toLowerCase();
        matches = matches && (
          cls.course.classType.toLowerCase().includes(lowerCaseName) ||
          cls.assignedInstructor.toLowerCase().includes(lowerCaseName)
        );
      }
      if (dayOfWeek) {
        matches = matches && cls.course.dayOfWeek.toLowerCase() === dayOfWeek.toLowerCase();
      }
      if (timeOfDay && cls.course.time) {
        const [hour] = cls.course.time.split(':').map(Number);
        switch (timeOfDay) {
          case 'morning':
            matches = matches && hour >= 6 && hour < 12;
            break;
          case 'afternoon':
            matches = matches && hour >= 12 && hour < 18;
            break;
          case 'evening':
            matches = matches && hour >= 18 && hour < 22;
            break;
        }
      }
      if (courseFirebaseKey) {
        matches = matches && cls.course.firebaseKey === courseFirebaseKey;
      }
      return matches;
    });
  }, [allClasses, filters]);

  const refresh = useCallback(() => {
    fetchClasses();
  }, [fetchClasses]);

  return { 
    classes: filteredClasses, 
    loading, 
    refresh, 
    setFilters 
  };
}

export function useYogaCourses() {
  const [courses, setCourses] = useState<YogaCourse[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  const fetchCourses = useCallback(async () => {
    try {
      setLoading({ isLoading: true });
      const fetchedCourses = await yogaService.getAllCourses();
      setCourses(fetchedCourses);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch courses' 
      });
    }
  }, [yogaService]);

  useEffect(() => {
    fetchCourses();
  }, [fetchCourses]);

  const refresh = useCallback(() => {
    fetchCourses();
  }, [fetchCourses]);

  return { courses, loading, refresh };
}

export function useClassDetail(classId: string | undefined) {
  const [classDetail, setClassDetail] = useState<YogaClass | null>(null);
  const [isBooked, setIsBooked] = useState(false);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  const fetchClassDetail = useCallback(async () => {
    if (!classId) {
      setLoading({ isLoading: false, error: 'Class ID is required' });
      return;
    }

    try {
      setLoading({ isLoading: true });
      const fetchedClass = await yogaService.getClassById(classId);
      const userBookings = await yogaService.getUserBookings();
      const alreadyBooked = userBookings.some(booking => booking.classId === classId);
      
      setClassDetail(fetchedClass);
      setIsBooked(alreadyBooked);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : 'Failed to fetch class details' 
      });
    }
  }, [classId, yogaService]);

  useEffect(() => {
    // Initial fetch
    fetchClassDetail();

    // Subscribe to real-time updates for all classes
    const unsubscribe = yogaService.subscribeToClassesUpdates(async (updatedClasses) => {
      try {
        const updatedClass = updatedClasses.find(cls => cls.firebaseKey === classId);
        if (updatedClass) {
          const course = await yogaService.getCourseById(updatedClass.courseFirebaseKey);
          setClassDetail({ ...updatedClass, course });
        }
      } catch (error) {
        // Handle potential errors during update
        console.error("Failed to process class updates:", error);
      }
    });

    return () => unsubscribe();
  }, [classId, yogaService, fetchClassDetail]);

  const refresh = useCallback(() => {
    fetchClassDetail();
  }, [fetchClassDetail]);

  return { classDetail, isBooked, loading, refresh };
}

export function useUserBookings() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const yogaService = YogaService.getInstance();

  useEffect(() => {
    setLoading({ isLoading: true });
    const unsubscribe = yogaService.subscribeToBookingsUpdates((updatedBookings) => {
      setBookings(updatedBookings);
      setLoading({ isLoading: false });
    });

    return () => unsubscribe();
  }, [yogaService]);

  const refresh = useCallback(async () => {
    setLoading({ isLoading: true });
    try {
      const userBookings = await yogaService.getUserBookings(); // One-time fetch for manual refresh
      setBookings(userBookings);
      setLoading({ isLoading: false });
    } catch (error) {
      setLoading({
        isLoading: false,
        error: error instanceof Error ? error.message : 'Failed to fetch bookings'
      });
    }
  }, [yogaService]);

  return { bookings, loading, refresh };
}
