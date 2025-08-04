/**
 * @file YogaClass.ts
 * @description Defines the type structure and utility functions for a YogaClass object.
 */
import { YogaCourse } from './YogaCourse';

/**
 * @interface YogaClass
 * @description Represents a specific, scheduled instance of a YogaCourse.
 */
export interface YogaClass {
  /** The original database ID (optional). */
  id?: number;
  
  /** Foreign key linking to the `YogaCourse` in Firebase. */
  courseFirebaseKey: string;
  
  /** The specific date of this class session (e.g., "dd/MM/yyyy"). */
  date: string;
  /** The name of the instructor assigned to this specific session. */
  assignedInstructor: string;
  
  /** Optional fields that can override or supplement the course template for this specific session. */
  additionalComments?: string;
  /** The specific capacity for this session, overriding the course default if set. */
  actualCapacity?: number;
  /** The number of available slots for booking. */
  slotsAvailable?: number;
  /** The current status of the class. */
  status?: 'active' | 'completed' | 'cancelled';
  
  /** The timestamp when the class was created. */
  createdDate?: number;
  
  /** The unique key for this class object in Firebase. */
  firebaseKey?: string;
  
  /** The full `YogaCourse` object, populated when needed for displaying details. */
  course?: YogaCourse;
}

// Utility functions for class data

/**
 * @function getDayOfWeek
 * @description Calculates the day of the week from a date string.
 * @param {string} dateString - The date in "dd/MM/yyyy" format.
 * @returns {string} The full name of the day of the week (e.g., "Monday").
 */
export const getDayOfWeek = (dateString: string): string => {
  try {
    const [day, month, year] = dateString.split('/');
    const date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return date.toLocaleDateString('en-US', { weekday: 'long' });
  } catch (e) {
    return '';
  }
};

/**
 * @function getClassDisplayName
 * @description Creates a user-friendly display name for a yoga class.
 * @param {YogaClass} yogaClass - The yoga class object.
 * @returns {string} A formatted display name.
 */
export const getClassDisplayName = (yogaClass: YogaClass): string => {
  if (yogaClass.course) {
    return `${yogaClass.course.classType} - ${yogaClass.date}`;
  }
  return `Class Session - ${yogaClass.date}`;
};

/**
 * @function isPastClass
 * @description Checks if a class date is in the past.
 * @param {string} dateString - The date in "dd/MM/yyyy" format.
 * @returns {boolean} `true` if the class date is before today, `false` otherwise.
 */
export const isPastClass = (dateString: string): boolean => {
  try {
    const [day, month, year] = dateString.split('/');
    const classDate = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Set to the beginning of the day for accurate comparison
    return classDate < today;
  } catch (e) {
    return false;
  }
};

/**
 * @function getEffectiveCapacity
 * @description Determines the effective capacity of a class, using the specific override if available.
 * @param {YogaClass} yogaClass - The yoga class object.
 * @returns {number} The actual capacity or the default course capacity.
 */
export const getEffectiveCapacity = (yogaClass: YogaClass): number => {
  if (yogaClass.actualCapacity && yogaClass.actualCapacity > 0) {
    return yogaClass.actualCapacity;
  }
  return yogaClass.course?.capacity || 0;
};

/**
 * @function formatClassDate
 * @description Formats a class date string (currently returns it as is).
 * @param {string} dateString - The date string.
 * @returns {string} The formatted date string.
 */
export const formatClassDate = (dateString: string): string => {
  return dateString;
};

/**
 * @function formatCreatedDate
 * @description Formats a timestamp into a readable date and time string.
 * @param {number} timestamp - The Unix timestamp.
 * @returns {string} The formatted date and time string.
 */
export const formatCreatedDate = (timestamp: number): string => {
  return new Date(timestamp).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};
