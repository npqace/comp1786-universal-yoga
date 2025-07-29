import { ref, onValue, off, get, runTransaction, set } from 'firebase/database';
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

      // The map should be keyed by the firebaseKey of the course.
      const courseMap = new Map<string, YogaCourse>();
      courses.forEach(course => {
        if (course.firebaseKey) {
          courseMap.set(course.firebaseKey, course);
        }
      });

      return classes.map(cls => {
        // Look up the course using the courseFirebaseKey from the class.
        const course = courseMap.get(cls.courseFirebaseKey);
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
      const { name, dayOfWeek, timeOfDay, courseFirebaseKey } = filters;
      const classesWithCourses = await this.getClassesWithCourses();
      
      return classesWithCourses.filter(cls => {
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
   * Get a specific course by ID
   */
  async getCourseById(courseId: string): Promise<YogaCourse | null> {
    try {
      const courseRef = ref(database, `courses/${courseId}`);
      const snapshot = await get(courseRef);
      if (snapshot.exists()) {
        return { ...snapshot.val(), firebaseKey: courseId } as YogaCourse;
      }
      return null;
    } catch (error) {
      console.error('Error fetching course by ID:', error);
      throw new Error('Failed to fetch course');
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
      // First, get the details of the class being booked
      const classSnapshot = await get(classRef);
      if (!classSnapshot.exists()) {
        throw new Error("This class doesn't exist.");
      }
      const classData = classSnapshot.val() as YogaClass;

      // Check if the class is available for booking
      if (classData.status?.toLowerCase() !== 'active') {
        throw new Error('This class cannot be booked because it is either cancelled or completed.');
      }

      // Also, get the associated course to have all details
      const courseRef = ref(database, `courses/${classData.courseFirebaseKey}`);
      const courseSnapshot = await get(courseRef);
      if (!courseSnapshot.exists()) {
        throw new Error("The course for this class could not be found.");
      }
      const courseData = courseSnapshot.val() as YogaCourse;
      
      // Now, run the transaction to decrease the available slots
      await runTransaction(classRef, (currentData: YogaClass) => {
        if (currentData && typeof currentData.slotsAvailable === 'number') {
          if (currentData.slotsAvailable > 0) {
            currentData.slotsAvailable--;
          } else {
            // Abort transaction by returning undefined
            return; 
          }
        }
        return currentData;
      });

      // Create the rich, denormalized booking object
      const bookingId = `${user.uid}_${classId}`;
      const newBookingRef = ref(database, `bookings/${bookingId}`);
      
      const bookingData: Booking = {
        id: bookingId,
        userId: user.uid,
        classId: classId,
        bookingDate: new Date().toISOString(),
        // Denormalized data
        userName: user.displayName || 'Unknown User',
        userEmail: user.email || 'No Email',
        className: courseData.classType,
        classDate: classData.date,
        classTime: courseData.time,
      };

      // Save the new booking object
      await set(newBookingRef, bookingData);

      // Also, update the user-specific and class-specific booking records for easy lookups
      const userBookingRef = ref(database, `userBookings/${user.uid}/${classId}`);
      await set(userBookingRef, true);

      const classBookingRef = ref(database, `classBookings/${classId}/${user.uid}`);
      await set(classBookingRef, true);

    } catch (error) {
      console.error('Booking failed:', error);
      if (error instanceof Error) {
        throw new Error(`Failed to book the class: ${error.message}`);
      }
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

    // Get the IDs of the classes the user has booked
    const userBookingsRef = ref(database, `userBookings/${user.uid}`);
    const snapshot = await get(userBookingsRef);

    if (snapshot.exists()) {
      const classIds = Object.keys(snapshot.val());
      const bookings: Booking[] = [];

      // For each class ID, fetch the full booking object
      for (const classId of classIds) {
        const bookingRef = ref(database, `bookings/${user.uid}_${classId}`);
        const bookingSnapshot = await get(bookingRef);
        if (bookingSnapshot.exists()) {
          bookings.push(bookingSnapshot.val() as Booking);
        }
      }
      return bookings;
    }
    return [];
  }
} 