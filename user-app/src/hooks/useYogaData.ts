/**
 * @file useYogaData.ts
 * @description Custom hooks for fetching and managing yoga-related data from the YogaService.
 */
import { useState, useEffect, useCallback, useMemo } from 'react';
import { YogaService } from '../services/yogaService';
import { YogaClass, LoadingState, SearchFilters, Booking } from '../types';
import { YogaCourse } from '../types/YogaCourse';

/**
 * @hook useFilteredYogaClasses
 * @description A hook to fetch all yoga classes, apply filters, and manage loading/error states.
 * It also subscribes to real-time updates for classes.
 * @returns {{classes: YogaClass[], loading: LoadingState, refresh: () => void, setFilters: (filters: SearchFilters) => void}}
 */
export function useFilteredYogaClasses() {
  const [allClasses, setAllClasses] = useState<YogaClass[]>([]);
  const [loading, setLoading] = useState<LoadingState>({ isLoading: true });
  const [filters, setFilters] = useState<SearchFilters>({});
  const yogaService = YogaService.getInstance();

  /**
   * @callback fetchClasses
   * @description Fetches all classes with their associated course data.
   */
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

  /**
   * @effect
   * @description Fetches initial data and subscribes to real-time class updates.
   */
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

  /**
   * @memo filteredClasses
   * @description Memoized calculation of filtered classes based on the current filters.
   * This avoids re-calculating on every render unless classes or filters change.
   */
  const filteredClasses = useMemo(() => {
    const { name, dayOfWeek, timeOfDay, courseFirebaseKey } = filters;
    
    let classesToFilter = [...allClasses];

    // Apply filters if any are set
    if (name || dayOfWeek || timeOfDay || courseFirebaseKey) {
      classesToFilter = classesToFilter.filter(cls => {
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
    }

    // Sort the classes by date
    return classesToFilter.sort((a, b) => {
      const dateA = new Date(a.date.split('/').reverse().join('-'));
      const dateB = new Date(b.date.split('/').reverse().join('-'));
      return dateA.getTime() - dateB.getTime();
    });
  }, [allClasses, filters]);

  /**
   * @callback refresh
   * @description Manually triggers a refetch of the class data.
   */
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

/**
 * @hook useYogaCourses
 * @description A hook to fetch all yoga courses and manage loading/error states.
 * @returns {{courses: YogaCourse[], loading: LoadingState, refresh: () => void}}
 */
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

/**
 * @hook useClassDetail
 * @description A hook to fetch the details of a specific yoga class by its ID.
 * It also determines if the current user has booked the class.
 * @param {string | undefined} classId - The ID of the class to fetch.
 * @returns {{classDetail: YogaClass | null, isBooked: boolean, loading: LoadingState, refresh: () => void}}
 */
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

    // Subscribe to real-time updates for all classes to catch changes to this specific class
    const unsubscribe = yogaService.subscribeToClassesUpdates(async (updatedClasses) => {
      try {
        const updatedClass = updatedClasses.find(cls => cls.firebaseKey === classId);
        if (updatedClass) {
          // If the class is found in the update, re-fetch its course details and update the state
          const course = await yogaService.getCourseById(updatedClass.courseFirebaseKey);
          setClassDetail({ ...updatedClass, course });
        }
      } catch (error) {
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

/**
 * @hook useUserBookings
 * @description A hook to fetch the current user's bookings and listen for real-time updates.
 * It enriches booking data with real-time class status.
 * @returns {{bookings: Booking[], loading: LoadingState, refresh: () => Promise<void>}}
 */
export function useUserBookings() {
  // The final, combined data that will be sent to the UI
  const [enrichedBookings, setEnrichedBookings] = useState<Booking[]>([]);
  
  // Raw data streams from Firebase, initialized to null to track loading state
  const [rawBookings, setRawBookings] = useState<Booking[] | null>(null);
  const [classMap, setClassMap] = useState<Map<string, YogaClass> | null>(null);

  const [error, setError] = useState<string | undefined>();
  const yogaService = YogaService.getInstance();

  // Effect for setting up Firebase subscriptions for both classes and user bookings
  useEffect(() => {
    const unsubscribeClasses = yogaService.subscribeToClassesUpdates((updatedClasses) => {
      const newClassMap = new Map<string, YogaClass>();
      updatedClasses.forEach(cls => {
        if (cls.firebaseKey) {
          newClassMap.set(cls.firebaseKey, cls);
        }
      });
      setClassMap(newClassMap);
    });

    const unsubscribeBookings = yogaService.subscribeToBookingsUpdates((updatedBookings) => {
      setRawBookings(updatedBookings);
    });

    return () => {
      unsubscribeClasses();
      unsubscribeBookings();
    };
  }, [yogaService]);

  // Effect for combining data when either raw stream updates
  useEffect(() => {
    // We only proceed if both data sources have been loaded at least once
    if (rawBookings !== null && classMap !== null) {
      const combinedData = rawBookings.map(booking => {
        const yogaClass = classMap.get(booking.classId);
        return {
          ...booking,
          // Prioritize the live, real-time status from the class map,
          // but fall back to the status saved on the booking itself.
          classStatus: yogaClass?.status || booking.classStatus,
        };
      });
      setEnrichedBookings(combinedData);
    }
  }, [rawBookings, classMap]);

  /**
   * @callback refresh
   * @description Manually re-fetches all booking and class data.
   */
  const refresh = useCallback(async () => {
    setRawBookings(null);
    setClassMap(null);
    setError(undefined);
    try {
      const [userBookings, classesWithCourses] = await Promise.all([
        yogaService.getUserBookings(),
        yogaService.getClassesWithCourses()
      ]);
      
      const newClassMap = new Map<string, YogaClass>();
      classesWithCourses.forEach(cls => {
        if (cls.firebaseKey) {
          newClassMap.set(cls.firebaseKey, cls);
        }
      });

      setRawBookings(userBookings);
      setClassMap(newClassMap);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to refresh bookings');
    }
  }, [yogaService]);

  return { 
    bookings: enrichedBookings, 
    loading: { 
      // The screen is in a loading state until both streams have returned data
      isLoading: rawBookings === null || classMap === null, 
      error 
    }, 
    refresh 
  };
}