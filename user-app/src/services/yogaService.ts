import { ref, onValue, off, get, runTransaction } from 'firebase/database';
import { database, auth } from './firebase';
import { YogaClass, SearchFilters, Booking } from '../types';
import { YogaCourse } from '../types/YogaCourse';

export class YogaService {
  private static instance: YogaService;

  public static getInstance(): YogaService {
    if (!YogaService.instance) {
      YogaService.instance = new YogaService();
    }
    return YogaService.instance;
  }

  /**
   * Fetch all yoga courses from Firebase
   */
  async getAllCourses(): Promise<YogaCourse[]> {
    try {
      const coursesRef = ref(database, 'courses');
      const snapshot = await get(coursesRef);
      
      if (snapshot.exists()) {
        const coursesData = snapshot.val();
        const courses = Object.keys(coursesData).map(key => ({
          ...coursesData[key],
          firebaseKey: key
        }));
        return courses;
      }
      return [];
    } catch (error) {
      console.error('Error fetching courses:', error);
      throw new Error('Failed to fetch courses');
    }
  }

  /**
   * Fetch all yoga classes from Firebase
   */
  async getAllClasses(): Promise<YogaClass[]> {
    try {
      const classesRef = ref(database, 'classes');
      const snapshot = await get(classesRef);
      
      if (snapshot.exists()) {
        const classesData = snapshot.val();
        if (Array.isArray(classesData)) {
          return classesData.filter(cls => cls !== null);
        }
        return Object.keys(classesData).map(key => ({
          ...classesData[key],
          firebaseKey: key
        }));
      }
      return [];
    } catch (error) {
      console.error('Error fetching classes:', error);
      throw new Error('Failed to fetch classes');
    }
  }

  /**
   * Get classes with their associated course data.
   */
  async getClassesWithCourses(): Promise<YogaClass[]> {
    try {
      const [courses, classes] = await Promise.all([
        this.getAllCourses(),
        this.getAllClasses()
      ]);

      // The map should be keyed by the numeric `id` of the course.
      const courseMap = new Map<number, YogaCourse>();
      courses.forEach(course => {
        if (course.id) {
          courseMap.set(course.id, course);
        }
      });

      return classes.map(cls => {
        // Look up the course using the numeric `courseId` from the class.
        const course = courseMap.get(cls.courseId);
        return {
          ...cls,
          course: course
        };
      });
    } catch (error) {
      console.error('Error fetching classes with courses:', error);
      throw new Error('Failed to fetch classes with course data');
    }
  }

  /**
   * Search classes by day of week and/or time
   */
  async searchClasses(filters: SearchFilters): Promise<YogaClass[]> {
    try {
      const { name, dayOfWeek, timeOfDay, courseId } = filters;
      const classesWithCourses = await this.getClassesWithCourses();
      
      return classesWithCourses.filter(cls => {
        if (!cls.course) return false;
        let matches = true;
        if (name) {
          matches = matches && cls.course.classType.toLowerCase().includes(name.toLowerCase());
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
        if (courseId) {
          // Compare the courseId from the filter with the firebaseKey of the class's course
          matches = matches && cls.course.firebaseKey === courseId;
        }
        return matches;
      });
    } catch (error) {
      console.error('Error searching classes:', error);
      throw new Error('Failed to search classes');
    }
  }

  /**
   * Get a specific class by ID
   */
  async getClassById(classId: string): Promise<YogaClass | null> {
    try {
      const classes = await this.getClassesWithCourses();
      return classes.find(cls => cls.firebaseKey === classId || cls.id?.toString() === classId) || null;
    } catch (error) {
      console.error('Error fetching class by ID:', error);
      throw new Error('Failed to fetch class');
    }
  }

  /**
   * Subscribe to real-time updates for classes
   */
  subscribeToClassesUpdates(callback: (classes: YogaClass[]) => void): () => void {
    const classesRef = ref(database, 'classes');
    
    const unsubscribe = onValue(classesRef, (snapshot) => {
      if (snapshot.exists()) {
        const classesData = snapshot.val();
        let classes: YogaClass[];
        
        if (Array.isArray(classesData)) {
          classes = classesData.filter(cls => cls !== null);
        } else {
          classes = Object.keys(classesData).map(key => ({
            ...classesData[key],
            firebaseKey: key
          }));
        }
        callback(classes);
      } else {
        callback([]);
      }
    });

    return () => off(classesRef, 'value', unsubscribe);
  }

  /**
   * Book a class for the current user
   */
  async bookClass(classId: string): Promise<void> {
    const user = auth.currentUser;
    if (!user) {
      throw new Error('You must be logged in to book a class.');
    }

    const classRef = ref(database, `classes/${classId}`);

    try {
      await runTransaction(classRef, (currentData: YogaClass) => {
        if (currentData && typeof currentData.slotsAvailable === 'number') {
          if (currentData.slotsAvailable > 0) {
            currentData.slotsAvailable--;
          } else {
            return; // Abort transaction
          }
        }
        return currentData;
      });

      const newBookingRef = ref(database, `bookings/${user.uid}_${classId}`);
      const userBookingRef = ref(database, `userBookings/${user.uid}/${classId}`);
      const classBookingRef = ref(database, `classBookings/${classId}/${user.uid}`);

      const bookingData: Booking = {
        id: `${user.uid}_${classId}`,
        userId: user.uid,
        classId: classId,
        bookingDate: new Date().toISOString(),
      };

      const snapshot = await get(newBookingRef);
      if (!snapshot.exists()) {
        await Promise.all([
            runTransaction(newBookingRef, () => bookingData),
            runTransaction(userBookingRef, () => true),
            runTransaction(classBookingRef, () => true),
        ]);
      }

    } catch (error) {
      console.error('Booking failed:', error);
      throw new Error('Failed to book the class. Please try again.');
    }
  }

  /**
   * Get all bookings for the current user
   */
  async getUserBookings(): Promise<Booking[]> {
    const user = auth.currentUser;
    if (!user) {
      return [];
    }

    const userBookingsRef = ref(database, `userBookings/${user.uid}`);
    const snapshot = await get(userBookingsRef);

    if (snapshot.exists()) {
      const bookingIds = Object.keys(snapshot.val());
      const bookings: Booking[] = [];

      for (const classId of bookingIds) {
        const bookingRef = ref(database, `bookings/${user.uid}_${classId}`);
        const bookingSnapshot = await get(bookingRef);
        if (bookingSnapshot.exists()) {
          bookings.push(bookingSnapshot.val());
        }
      }
      return bookings;
    }
    return [];
  }
} 