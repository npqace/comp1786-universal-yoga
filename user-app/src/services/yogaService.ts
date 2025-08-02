import { ref, onValue, off, get, runTransaction, set, update } from 'firebase/database';
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
  private async getAllClasses(): Promise<YogaClass[]> {
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
   * Subscribe to real-time updates for a user's bookings
   */
  subscribeToBookingsUpdates(callback: (bookings: Booking[]) => void): () => void {
    const user = auth.currentUser;
    if (!user) {
      callback([]);
      return () => {}; // Return an empty unsubscribe function
    }

    const userBookingsRef = ref(database, `userBookings/${user.uid}`);
    let activeBookingListeners: { ref: any; listener: any }[] = [];

    const mainListener = onValue(userBookingsRef, (snapshot) => {
      // Clear previous listeners to handle booking cancellations
      activeBookingListeners.forEach(({ ref, listener }) => off(ref, 'value', listener));
      activeBookingListeners = [];

      if (snapshot.exists()) {
        const classIds = Object.keys(snapshot.val());
        const bookings: { [key: string]: Booking } = {};

        if (classIds.length === 0) {
          callback([]);
          return;
        }

        classIds.forEach((classId) => {
          const bookingId = `${user.uid}_${classId}`;
          const bookingRef = ref(database, `bookings/${bookingId}`);
          
          const bookingListener = onValue(bookingRef, (bookingSnapshot) => {
            if (bookingSnapshot.exists()) {
              bookings[bookingId] = bookingSnapshot.val();
              callback(Object.values(bookings).sort((a, b) => new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime()));
            } else {
              delete bookings[bookingId];
              callback(Object.values(bookings).sort((a, b) => new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime()));
            }
          });

          activeBookingListeners.push({ ref: bookingRef, listener: bookingListener });
        });
      } else {
        callback([]);
      }
    });

    return () => {
      off(userBookingsRef, 'value', mainListener);
      activeBookingListeners.forEach(({ ref, listener }) => off(ref, 'value', listener));
    };
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
        price: courseData.price,
        classStatus: classData.status,
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
   * Cancel a booking for the current user
   */
  async cancelBooking(classId: string): Promise<void> {
    const user = auth.currentUser;
    if (!user) {
      throw new Error('You must be logged in to cancel a booking.');
    }

    const classRef = ref(database, `classes/${classId}`);
    const bookingId = `${user.uid}_${classId}`;
    const bookingRef = ref(database, `bookings/${bookingId}`);
    const userBookingRef = ref(database, `userBookings/${user.uid}/${classId}`);
    const classBookingRef = ref(database, `classBookings/${classId}/${user.uid}`);

    try {
      // Use a transaction to safely increment the available slots
      await runTransaction(classRef, (currentData: YogaClass) => {
        if (currentData && typeof currentData.slotsAvailable === 'number') {
          currentData.slotsAvailable++;
        }
        return currentData;
      });

      // Use a multi-path update to remove all booking-related data atomically
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

  /**
   * Update denormalized user data across the database
   */
  async updateDenormalizedUserData(userId: string, newName: string): Promise<void> {
    try {
      const updates: { [key: string]: any } = {};

      // 1. Find all of the user's bookings to update them
      const userBookingsRef = ref(database, `userBookings/${userId}`);
      const userBookingsSnapshot = await get(userBookingsRef);

      if (userBookingsSnapshot.exists()) {
        const classIds = Object.keys(userBookingsSnapshot.val());
        for (const classId of classIds) {
          updates[`/bookings/${userId}_${classId}/userName`] = newName;
        }
      }

      // 2. Update the user's name in the top-level 'users' collection
      updates[`/users/${userId}/displayName`] = newName;
      
      // 3. Perform a single, atomic multi-path update
      const dbRef = ref(database);
      await update(dbRef, updates);

    } catch (error) {
      console.error('Error updating denormalized user data:', error);
      // We don't re-throw the error to the user, as the primary action (profile update) was successful.
      // We can add more robust error logging here if needed.
    }
  }
} 