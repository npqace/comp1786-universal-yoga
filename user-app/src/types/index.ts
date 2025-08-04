/**
 * @file index.ts
 * @description Barrel file for exporting all types and common interfaces.
 * This allows for cleaner imports in other parts of the application.
 * e.g., `import { YogaClass, User } from '../types';`
 */

// Export utility functions and types from their respective files
export { formatCreatedDate, formatPrice } from './YogaCourse';
export * from './YogaClass';
export * from './User';
export * from './Booking';

/**
 * @interface SearchFilters
 * @description Defines the shape of the filters object used for searching classes.
 */
export interface SearchFilters {
  name?: string;
  dayOfWeek?: string;
  timeOfDay?: string;
  courseFirebaseKey?: string;
}

/**
 * @interface LoadingState
 * @description A standardized interface for managing loading and error states in hooks.
 */
export interface LoadingState {
  isLoading: boolean;
  error?: string;
}

// --- Navigation Types ---

/**
 * @type RootStackParamList
 * @description Defines the parameters for each screen in the main stack navigator.
 * `undefined` means the route takes no parameters.
 */
export type RootStackParamList = {
  Home: undefined;
  ClassList: undefined;
  Search: undefined;
  ClassDetail: { classId: string; courseFirebaseKey?: string };
};

/**
 * @type TabParamList
 * @description Defines the parameters for each screen in the bottom tab navigator.
 */
export type TabParamList = {
  Classes: undefined;
  Search: undefined;
};