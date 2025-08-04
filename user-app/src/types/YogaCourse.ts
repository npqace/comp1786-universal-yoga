/**
 * @file YogaCourse.ts
 * @description Defines the type structure and utility functions for a YogaCourse object.
 */

/**
 * @interface YogaCourse
 * @description Represents a template for a recurring yoga class.
 * It contains the default properties for classes of this type.
 */
export interface YogaCourse {
  /** The original database ID (optional). */
  id?: number;
  
  /** The default day of the week for this course. */
  dayOfWeek: string;
  /** The default time for this course. */
  time: string;
  /** The default maximum number of participants. */
  capacity: number;
  /** The duration of the class in minutes. */
  duration: number;
  /** The price for a single session of this course. */
  price: number;
  /** The name or type of the class (e.g., "Vinyasa Flow", "Hatha Yoga"). */
  classType: string;
  
  /** Optional fields describing the course. */
  description?: string;
  /** The default instructor's name. */
  instructorName?: string;
  /** The default room number. */
  roomNumber?: string;
  /** The difficulty level (e.g., "Beginner", "Intermediate"). */
  difficultyLevel?: string;
  /** Any equipment needed for the class. */
  equipmentNeeded?: string;
  /** The target age group for the class. */
  ageGroup?: string;
  
  /** The timestamp when the course was created. */
  createdDate?: number;
  
  /** The unique key for this course object in Firebase. */
  firebaseKey?: string;
}

// Utility functions for course data

/**
 * @function formatPrice
 * @description Formats a number into a currency string.
 * @param {number} price - The price to format.
 * @returns {string} The formatted price string (e.g., "£50.00").
 */
export const formatPrice = (price: number): string => {
  return `£${price.toFixed(2)}`;
};

/**
 * @function formatDuration
 * @description Formats a duration in minutes into a readable string.
 * @param {number} duration - The duration in minutes.
 * @returns {string} The formatted duration string (e.g., "60 minutes").
 */
export const formatDuration = (duration: number): string => {
  return `${duration} minutes`;
};

/**
 * @function getCourseDisplayName
 * @description Creates a user-friendly display name for a yoga course.
 * @param {YogaCourse} course - The yoga course object.
 * @returns {string} A formatted display name.
 */
export const getCourseDisplayName = (course: YogaCourse): string => {
  return `${course.classType} - ${course.dayOfWeek} at ${course.time}`;
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