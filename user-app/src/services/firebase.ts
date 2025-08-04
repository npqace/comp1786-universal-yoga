/**
 * @file firebase.ts
 * @description Firebase configuration and initialization.
 */
import { initializeApp, getApp, getApps, FirebaseApp } from 'firebase/app';
import { 
  getAuth,
  initializeAuth, 
  getReactNativePersistence, 
  createUserWithEmailAndPassword,
  updateProfile,
  Auth
} from 'firebase/auth';
import { getDatabase, ref, set, Database } from 'firebase/database';
import ReactNativeAsyncStorage from '@react-native-async-storage/async-storage';
import { User } from '../types';

// Firebase configuration object from your Firebase project settings.
const firebaseConfig = {
  apiKey: "AIzaSyAb21mpled_3NJiQqmN1wgIYwQnWgPCGs8",
  authDomain: "universal-yoga-3e267.firebaseapp.com",
  databaseURL: "https://universal-yoga-3e267-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "universal-yoga-3e267",
  storageBucket: "universal-yoga-3e267.firebasestorage.app",
  messagingSenderId: "284957734317",
  appId: "1:284957734317:web:cdc0a2c091a1a4dd4c1d24"
};

// Declare variables for Firebase services.
let app: FirebaseApp;
let auth: Auth;
let database: Database;

// Initialize Firebase only if it hasn't been initialized yet.
if (!getApps().length) {
  try {
    app = initializeApp(firebaseConfig);
    // Initialize Auth with persistence for React Native.
    auth = initializeAuth(app, {
      persistence: getReactNativePersistence(ReactNativeAsyncStorage),
    });
  } catch (error) {
    // This can happen in certain edge cases with hot-reloading.
    console.error("Firebase initialization error", error);
    app = getApp();
    auth = getAuth(app);
  }
} else {
  // If already initialized, get the existing app and auth instances.
  app = getApp();
  auth = getAuth(app);
}

// Get a reference to the Firebase Realtime Database.
database = getDatabase(app);


/**
 * @function signUp
 * @description Creates a new user account, updates their profile with a display name,
 * and saves their public information to the Realtime Database.
 * @param {string} name - The user's display name.
 * @param {string} email - The user's email address.
 * @param {string} password - The user's password.
 * @returns {Promise<import('firebase/auth').UserCredential>} The user credential object.
 */
export const signUp = async (name: string, email: string, password: string) => {
  // Create the user with email and password.
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;

  // Update the user's profile in Firebase Authentication with their name.
  await updateProfile(user, { displayName: name });

  // Save the user's public data to the Realtime Database for easy access by other services.
  const userRef = ref(database, `users/${user.uid}`);
  const userData: User = {
    uid: user.uid,
    email: user.email,
    displayName: name,
  };
  await set(userRef, userData);

  return userCredential;
};

// Export the initialized Firebase services for use in other parts of the app.
export { app, auth, database };
