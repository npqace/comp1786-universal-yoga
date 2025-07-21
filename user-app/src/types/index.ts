export { formatCreatedDate, formatPrice } from './YogaCourse';
export * from './YogaClass';

// Common types for the app
export interface SearchFilters {
  name?: string;
  dayOfWeek?: string;
  timeOfDay?: string;
}

export interface LoadingState {
  isLoading: boolean;
  error?: string;
}

// Navigation types
export type RootStackParamList = {
  Home: undefined;
  ClassList: undefined;
  Search: undefined;
  ClassDetail: { classId: string; courseId?: string };
};

export type TabParamList = {
  Classes: undefined;
  Search: undefined;
}; 