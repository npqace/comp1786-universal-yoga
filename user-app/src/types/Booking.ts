export interface Booking {
  id: string; // The booking ID
  userId: string;
  classId: string;
  bookingDate: string; // The date the booking was made

  // Denormalized data for easier display
  userName?: string;
  userEmail?: string;
  className?: string;
  classDate?: string;
  classTime?: string;
  price?: number;
  classStatus?: 'active' | 'completed' | 'cancelled';
}