/**
 * @file yogaService.ts
 * @description A singleton service class to handle all interactions with the Firebase Realtime Database.
 */
import { ref, onValue, off, get, runTransaction, set, update } from 'firebase/database';
import { database, auth } from './firebase';
import { YogaClass, SearchFilters, Booking } from '../types';
import { YogaCourse } from '../types/YogaCourse';

/**
 * @class YogaService
 * @description Provides methods for fetching, creating, and updating yoga-related data in Firebase.
 * Implemented as a singleton to ensure a single instance throughout the app.
 */
export class YogaService {
  private static instance: YogaService;

  /**
   * @method getInstance
   * @description Gets the singleton instance of the YogaService.
   * @returns {YogaService} The singleton instance.
   */
  public static getInstance(): YogaService {
    if (!YogaService.instance) {
      YogaService.instance = new YogaService();
    }
    return YogaService.instance;
  }

  /**
   * @method getAllCourses
   * @description Fetches all yoga courses from the 'courses' collection in Firebase.
   * @returns {Promise<YogaCourse[]>} A promise that resolves to an array of yoga courses.
   */
  async getAllCourses(): Promise<YogaCourse[]> {
    try {
      const coursesRef = ref(database, 'courses');
      const snapshot = await get(coursesRef);
      
      if (snapshot.exists()) {
        const coursesData = snapshot.val();
        // Convert the snapshot object to an array and include the Firebase key.
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
   * @method getAllClasses
   * @description Fetches all yoga classes from the 'classes' collection in Firebase.
   * This is a private method used internally.
   * @private
   * @returns {Promise<YogaClass[]>} A promise that resolves to an array of yoga classes.
   */
  private async getAllClasses(): Promise<YogaClass[]> {
    try {
      const classesRef = ref(database, 'classes');
      const snapshot = await get(classesRef);
      
      if (snapshot.exists()) {
        const classesData = snapshot.val();
        // Handle both array and object data structures from Firebase.
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
   * @method getClassesWithCourses
   * @description Fetches all classes and enriches them with their corresponding course data.
   * This is an efficient way to get combined data in one go.
   * @returns {Promise<YogaClass[]>} A promise that resolves to an array of yoga classes with course data embedded.
   */
  async getClassesWithCourses(): Promise<YogaClass[]> {
    try {
      const [courses, classes] = await Promise.all([
        this.getAllCourses(),
        this.getAllClasses()
      ]);

      // Create a map of courses for efficient lookup.
      const courseMap = new Map<string, YogaCourse>();
      courses.forEach(course => {
        if (course.firebaseKey) {
          courseMap.set(course.firebaseKey, course);
        }
      });

      // Map over classes and attach the corresponding course object.
      return classes.map(cls => {
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
   * @method getClassById
   * @description Fetches a single class by its ID and enriches it with course data.
   * @param {string} classId - The ID of the class to fetch.
   * @returns {Promise<YogaClass | null>} A promise that resolves to the class object or null if not found.
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
   * @method getCourseById
   * @description Fetches a single course by its ID.
   * @param {string} courseId - The ID of the course to fetch.
   * @returns {Promise<YogaCourse | null>} A promise that resolves to the course object or null if not found.
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
   * @method subscribeToClassesUpdates
   * @description Subscribes to real-time updates for the 'classes' collection.
   * @param {(classes: YogaClass[]) => void} callback - The function to call with the updated classes.
   * @returns {() => void} An unsubscribe function to detach the listener.
   */
  subscribeToClassesUpdates(callback: (classes: YogaClass[]) => void): () => void {
    const classesRef = ref(database, 'classes');
    
    const listener = onValue(classesRef, (snapshot) => {
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

    return () => off(classesRef, 'value', listener);
  }

  /**
   * @method subscribeToBookingsUpdates
   * @description Subscribes to real-time updates for the current user's bookings.
   * @param {(bookings: Booking[]) => void} callback - The function to call with the updated bookings.
   * @returns {() => void} An unsubscribe function to detach the listeners.
   */
  subscribeToBookingsUpdates(callback: (bookings: Booking[]) => void): () => void {
    const user = auth.currentUser;
    if (!user) {
      callback([]);
      return () => {}; // Return an empty unsubscribe function if no user is logged in.
    }

    const userBookingsRef = ref(database, `userBookings/${user.uid}`);
    let activeBookingListeners: { ref: any; listener: any }[] = [];

    const mainListener = onValue(userBookingsRef, (snapshot) => {
      // Clear previous listeners to handle booking cancellations correctly.
      activeBookingListeners.forEach(({ ref, listener }) => off(ref, 'value', listener));
      activeBookingListeners = [];

      if (snapshot.exists()) {
        const classIds = Object.keys(snapshot.val());
        const bookings: { [key: string]: Booking } = {};

        if (classIds.length === 0) {
          callback([]);
          return;
        }

        // For each booked class ID, listen to the full booking object.
        classIds.forEach((classId) => {
          const bookingId = `${user.uid}_${classId}`;
          const bookingRef = ref(database, `bookings/${bookingId}`);
          
          const bookingListener = onValue(bookingRef, (bookingSnapshot) => {
            if (bookingSnapshot.exists()) {
              bookings[bookingId] = bookingSnapshot.val();
            } else {
              delete bookings[bookingId];
            }
            // Return the sorted list of bookings.
            callback(Object.values(bookings).sort((a, b) => new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime()));
          });

          activeBookingListeners.push({ ref: bookingRef, listener: bookingListener });
        });
      } else {
        callback([]);
      }
    });

    // Return a function to clean up all listeners.
    return () => {
      off(userBookingsRef, 'value', mainListener);
      activeBookingListeners.forEach(({ ref, listener }) => off(ref, 'value', listener));
    };
  }

  /**
   * @method bookClass
   * @description Books a class for the current user. This involves a transaction to ensure atomicity.
   * @param {string} classId - The ID of the class to book.
   * @returns {Promise<void>}
   */
  async bookClass(classId: string): Promise<void> {
    const user = auth.currentUser;
    if (!user) {
      throw new Error('You must be logged in to book a class.');
    }

    const classRef = ref(database, `classes/${classId}`);

    try {
      // Get class and course details to create a rich booking object.
      const classSnapshot = await get(classRef);
      if (!classSnapshot.exists()) {
        throw new Error("This class doesn't exist.");
      }
      const classData = classSnapshot.val() as YogaClass;

      if (classData.status?.toLowerCase() !== 'active') {
        throw new Error('This class cannot be booked because it is either cancelled or completed.');
      }

      const courseRef = ref(database, `courses/${classData.courseFirebaseKey}`);
      const courseSnapshot = await get(courseRef);
      if (!courseSnapshot.exists()) {
        throw new Error("The course for this class could not be found.");
      }
      const courseData = courseSnapshot.val() as YogaCourse;
      
      // Use a transaction to safely decrement the available slots.
      await runTransaction(classRef, (currentData: YogaClass) => {
        if (currentData && typeof currentData.slotsAvailable === 'number') {
          if (currentData.slotsAvailable > 0) {
            currentData.slotsAvailable--;
          } else {
            // Abort transaction by returning undefined if no slots are available.
            return; 
          }
        }
        return currentData;
      });

      // Create a denormalized booking object for efficient data retrieval.
      const bookingId = `${user.uid}_${classId}`;
      const newBookingRef = ref(database, `bookings/${bookingId}`);
      
      const bookingData: Booking = {
        id: bookingId,
        userId: user.uid,
        classId: classId,
        bookingDate: new Date().toISOString(),
        // Denormalized data for easier display in the bookings list.
        userName: user.displayName || 'Unknown User',
        userEmail: user.email || 'No Email',
        className: courseData.classType,
        classDate: classData.date,
        classTime: courseData.time,
        price: courseData.price,
        classStatus: classData.status,
      };

      // Save the new booking object and update lookup tables.
      await set(newBookingRef, bookingData);
      await set(ref(database, `userBookings/${user.uid}/${classId}`), true);
      await set(ref(database, `classBookings/${classId}/${user.uid}`), true);

    } catch (error) {
      console.error('Booking failed:', error);
      if (error instanceof Error) {
        throw new Error(`Failed to book the class: ${error.message}`);
      }
      throw new Error('Failed to book the class. Please try again.');
    }
  }

  /**
   * @method cancelBooking
   * @description Cancels a booking for the current user.
   * @param {string} classId - The ID of the class to cancel the booking for.
   * @returns {Promise<void>}
   */
  async cancelBooking(classId: string): Promise<void> {
    const user = auth.currentUser;
    if (!user) {
      throw new Error('You must be logged in to cancel a booking.');
    }

    const classRef = ref(database, `classes/${classId}`);
    const bookingId = `${user.uid}_${classId}`;

    try {
      // Use a transaction to safely increment the available slots.
      await runTransaction(classRef, (currentData: YogaClass) => {
        if (currentData && typeof currentData.slotsAvailable === 'number') {
          currentData.slotsAvailable++;
        }
        return currentData;
      });

      // Use a multi-path update to remove all booking-related data atomically.
      const updates: { [key: string]: null } = {};
      updates[`/bookings/${bookingId}`] = null;
      updates[`/userBookings/${user.uid}/${classId}`] = null;
      updates[`/classBookings/${classId}/${user.uid}`] = null;

      await update(ref(database), updates);

    } catch (error) {
      console.error('Cancellation failed:', error);
      if (error instanceof Error) {
        throw new Error(`Failed to cancel the booking: ${error.message}`);
      }
      throw new Error('Failed to cancel the booking. Please try again.');
    }
  }

  /**
   * @method getUserBookings
   * @description Fetches all bookings for the current user.
   * @returns {Promise<Booking[]>} A promise that resolves to an array of booking objects.
   */
  async getUserBookings(): Promise<Booking[]> {
    const user = auth.currentUser;
    if (!user) {
      return [];
    }

    const userBookingsRef = ref(database, `userBookings/${user.uid}`);
    const snapshot = await get(userBookingsRef);

    if (snapshot.exists()) {
      const classIds = Object.keys(snapshot.val());
      const bookings: Booking[] = [];

      // For each class ID, fetch the full booking object.
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

  /**
   * @method updateDenormalizedUserData
   * @description Updates denormalized user data (e.g., display name) across all relevant database locations.
   * @param {string} userId - The ID of the user to update.
   * @param {string} newName - The new display name.
   * @returns {Promise<void>}
   */
  async updateDenormalizedUserData(userId: string, newName: string): Promise<void> {
    try {
      const updates: { [key: string]: any } = {};

      // Find all of the user's bookings to update their name.
      const userBookingsRef = ref(database, `userBookings/${userId}`);
      const userBookingsSnapshot = await get(userBookingsRef);

      if (userBookingsSnapshot.exists()) {
        const classIds = Object.keys(userBookingsSnapshot.val());
        for (const classId of classIds) {
          updates[`/bookings/${userId}_${classId}/userName`] = newName;
        }
      }

      // Update the user's name in the top-level 'users' collection.
      updates[`/users/${userId}/displayName`] = newName;
      
      // Perform a single, atomic multi-path update.
      await update(ref(database), updates);

    } catch (error) {
      console.error('Error updating denormalized user data:', error);
      // We don't re-throw the error to the user, as the primary action (profile update) was successful.
      // More robust error logging can be added here if needed.
    }
  }
}