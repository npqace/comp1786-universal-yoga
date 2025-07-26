export interface YogaCourse {
  // Database ID
  id?: number;
  
  // Required fields for course template
  dayOfWeek: string;
  time: string;
  capacity: number;
  duration: number; // in minutes
  price: number;
  classType: string;
  
  // Optional fields for course template
  description?: string;
  instructorName?: string;
  roomNumber?: string;
  difficultyLevel?: string;
  equipmentNeeded?: string;
  ageGroup?: string;
  
  // Metadata fields
  createdDate?: number;
  
  // Firebase Key
  firebaseKey?: string;
}

// Utility functions for course data
export const formatPrice = (price: number): string => {
  return `£${price.toFixed(2)}`;
};

export const formatDuration = (duration: number): string => {
  return `${duration} minutes`;
};

export const getCourseDisplayName = (course: YogaCourse): string => {
  return `${course.classType} - ${course.dayOfWeek} at ${course.time}`;
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