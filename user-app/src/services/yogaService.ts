import { ref, onValue, off, get } from 'firebase/database';
import { database } from './firebase';
import { YogaClass, SearchFilters } from '../types';
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
      console.log('📚 Attempting to fetch courses from Firebase...');
      const coursesRef = ref(database, 'courses');
      const snapshot = await get(coursesRef);
      
      console.log('📚 Firebase courses snapshot exists:', snapshot.exists());
      
      if (snapshot.exists()) {
        const coursesData = snapshot.val();
        console.log('📚 Raw courses data:', coursesData);
        console.log('📚 Courses data type:', typeof coursesData);
        
        // Convert the object to array
        const courses = Object.keys(coursesData).map(key => ({
          ...coursesData[key],
          firebaseKey: key
        }));
        console.log('📚 Converted courses array:', courses.length);
        console.log('📚 Sample course:', courses[0]);
        return courses;
      }
      console.log('📚 No courses found in Firebase');
      return [];
    } catch (error) {
      console.error('📚 Error fetching courses:', error);
      throw new Error('Failed to fetch courses');
    }
  }

  /**
   * Fetch all yoga classes from Firebase
   */
  async getAllClasses(): Promise<YogaClass[]> {
    try {
      console.log('🔥 Attempting to fetch classes from Firebase...');
      const classesRef = ref(database, 'classes');
      const snapshot = await get(classesRef);
      
      console.log('🔥 Firebase snapshot exists:', snapshot.exists());
      
      if (snapshot.exists()) {
        const classesData = snapshot.val();
        console.log('🔥 Raw Firebase data:', classesData);
        console.log('🔥 Data type:', typeof classesData);
        console.log('🔥 Is array:', Array.isArray(classesData));
        
        // If stored as array, return as is
        if (Array.isArray(classesData)) {
          const filteredClasses = classesData.filter(cls => cls !== null);
          console.log('🔥 Filtered array classes:', filteredClasses.length);
          return filteredClasses;
        }
        // If stored as object, convert to array
        const objectClasses = Object.keys(classesData).map(key => ({
          ...classesData[key],
          firebaseKey: key
        }));
        console.log('🔥 Object converted to array:', objectClasses.length);
        return objectClasses;
      }
      
      console.log('🔥 No data found in Firebase');
      return [];
    } catch (error) {
      console.error('🔥 Error fetching classes:', error);
      throw new Error('Failed to fetch classes');
    }
  }

  /**
   * Get classes with their associated course data.
   *
   * The classes contain courseId references, so we need to fetch both
   * courses and classes, then join them together.
   */
  async getClassesWithCourses(): Promise<YogaClass[]> {
    try {
      console.log('🔗 Starting to fetch and join courses with classes...');
      
      const [courses, classes] = await Promise.all([
        this.getAllCourses(),
        this.getAllClasses()
      ]);

      console.log('🔗 Courses fetched:', courses.length);
      console.log('🔗 Classes fetched:', classes.length);
      console.log('🔗 Sample course:', courses[0]);

      // Create a map of course ID to course for quick lookup
      const courseMap = new Map<number, YogaCourse>();
      courses.forEach(course => {
        if (course.id) {
          courseMap.set(course.id, course);
          console.log(`🔗 Mapped course ID ${course.id} -> ${course.classType}`);
        }
      });

      // Attach course data to each class
      const classesWithCourses = classes.map(cls => {
        const course = courseMap.get(cls.courseId);
        console.log(`🔗 Class ${cls.id} (courseId: ${cls.courseId}) -> Course found: ${!!course}`);
        return {
          ...cls,
          course: course
        };
      });

      console.log('🔗 Final classes with courses:', classesWithCourses.length);
      return classesWithCourses;
    } catch (error) {
      console.error('🔗 Error fetching classes with courses:', error);
      throw new Error('Failed to fetch classes with course data');
    }
  }

  /**
   * Search classes by day of week and/or time
   */
  async searchClasses(filters: SearchFilters): Promise<YogaClass[]> {
    try {
      const { name, dayOfWeek, timeOfDay } = filters;
      const classesWithCourses = await this.getClassesWithCourses();
      
      return classesWithCourses.filter(cls => {
        if (!cls.course) return false;

        let matches = true;
        
        // Filter by name (case-insensitive)
        if (name) {
          matches = matches && cls.course.classType.toLowerCase().includes(name.toLowerCase());
        }

        // Filter by day of week
        if (dayOfWeek) {
          matches = matches && cls.course.dayOfWeek.toLowerCase() === dayOfWeek.toLowerCase();
        }
        
        // Filter by time of day
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
   * Subscribe to real-time updates for courses
   */
  subscribeToCoursesUpdates(callback: (courses: YogaCourse[]) => void): () => void {
    const coursesRef = ref(database, 'courses');
    
    const unsubscribe = onValue(coursesRef, (snapshot) => {
      if (snapshot.exists()) {
        const coursesData = snapshot.val();
        const courses = Object.keys(coursesData).map(key => ({
          ...coursesData[key],
          firebaseKey: key
        }));
        callback(courses);
      } else {
        callback([]);
      }
    });

    return () => off(coursesRef, 'value', unsubscribe);
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
} 