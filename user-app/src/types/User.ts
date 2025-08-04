/**
 * @file User.ts
 * @description Defines the type structure for a user object.
 */

/**
 * @interface User
 * @description Represents a user in the application.
 * This data is typically sourced from Firebase Authentication.
 */
export interface User {
  /** The unique identifier for the user from Firebase Auth. */
  uid: string;
  /** The user's email address. */
  email: string | null;
  /** The user's display name. */
  displayName: string | null;
}