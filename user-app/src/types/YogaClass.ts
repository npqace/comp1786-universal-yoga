import { YogaCourse } from './YogaCourse';

export interface YogaClass {
  // Database ID
  id?: number;
  
  // Foreign key to the course template
  courseId: number;
  
  // Required fields for specific class session
  date: string; // Format: "dd/MM/yyyy"
  assignedInstructor: string;
  
  // Optional fields for this specific session
  additionalComments?: string;
  actualCapacity?: number;
  slotsAvailable?: number;
  status?: 'scheduled' | 'cancelled' | 'completed';
  
  // Metadata fields
  createdDate?: number;
  
  // Firebase Key
  firebaseKey?: string;
  
  // Course reference (populated when needed)
  course?: YogaCourse;
}

// Utility functions for class data
export const getDayOfWeek = (dateString: string): string => {
  try {
    const [day, month, year] = dateString.split('/');
    const date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return date.toLocaleDateString('en-US', { weekday: 'long' });
  } catch (e) {
    return '';
  }
};

export const getClassDisplayName = (yogaClass: YogaClass): string => {
  if (yogaClass.course) {
    return `${yogaClass.course.classType} - ${yogaClass.date}`;
  }
  return `Class Session - ${yogaClass.date}`;
};

export const isPastClass = (dateString: string): boolean => {
  try {
    const [day, month, year] = dateString.split('/');
    const classDate = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return classDate < today;
  } catch (e) {
    return false;
  }
};

export const getEffectiveCapacity = (yogaClass: YogaClass): number => {
  if (yogaClass.actualCapacity && yogaClass.actualCapacity > 0) {
    return yogaClass.actualCapacity;
  }
  return yogaClass.course?.capacity || 0;
};

export const formatClassDate = (dateString: string): string => {
  return dateString;
};

export const formatCreatedDate = (timestamp: number): string => {
  return new Date(timestamp).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};