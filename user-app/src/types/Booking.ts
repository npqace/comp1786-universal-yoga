/**
 * @file Booking.ts
 * @description Defines the type structure for a booking object.
 */

/**
 * @interface Booking
 * @description Represents a single booking made by a user for a yoga class.
 * It includes denormalized data for efficient display in the UI.
 */
export interface Booking {
  /** The unique identifier for the booking (e.g., `userId_classId`). */
  id: string;
  /** The ID of the user who made the booking. */
  userId: string;
  /** The ID of the class that was booked. */
  classId: string;
  /** The ISO 8601 timestamp of when the booking was made. */
  bookingDate: string;

  // Denormalized data for easier display in lists and cards.
  /** The display name of the user. */
  userName?: string;
  /** The email of the user. */
  userEmail?: string;
  /** The name of the class. */
  className?: string;
  /** The date of the class. */
  classDate?: string;
  /** The time of the class. */
  classTime?: string;
  /** The price of the class at the time of booking. */
  price?: number;
  /** The status of the class ('active', 'completed', 'cancelled') at the time of booking or updated in real-time. */
  classStatus?: 'active' | 'completed' | 'cancelled';
}
